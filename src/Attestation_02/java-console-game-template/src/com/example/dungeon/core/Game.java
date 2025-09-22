package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.io.UncheckedIOException;

public class Game {
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private BufferedReader inputReader; // Для использования в команде fight

    static {
        WorldInfo.touch("Game");
    }

    public Game() {
        registerCommands();
        bootstrapWorld();
    }

    private void registerCommands() {
        commands.put("help", (ctx, a) -> System.out.println("Команды: " + String.join(", ", commands.keySet())));

        commands.put("gc-stats", (ctx, a) -> {
            Runtime rt = Runtime.getRuntime();
            long free = rt.freeMemory(), total = rt.totalMemory(), used = total - free;
            System.out.println("Память: used=" + used + " free=" + free + " total=" + total);
        });

        commands.put("look", (ctx, a) -> System.out.println(ctx.getCurrent().describe()));

        commands.put("move", (ctx, a) -> {
            if (a.isEmpty()) throw new InvalidCommandException("move: требуется направление (north/south/east/west)");
            String dir = a.get(0).toLowerCase(Locale.ROOT);
            Room cur = ctx.getCurrent();
            Room next = cur.getNeighbors().get(dir);

            if (next == null) throw new InvalidCommandException("Нельзя идти на " + dir + " отсюда.");

            // Проверка запертой двери
            if (next.isLocked()) {
                System.out.println("Дверь заперта! Используйте: " + next.getRequiredKey());
                return; // Просто выводим сообщение и выходим из команды
            }

            ctx.setCurrent(next);
            System.out.println("Вы перешли в " + next.getName());
            System.out.println(next.describe());
        });

        commands.put("take", (ctx, a) -> {
            if (a.isEmpty()) throw new InvalidCommandException("take: укажите имя предмета");
            String name = String.join(" ", a).trim();
            Room cur = ctx.getCurrent();
            Optional<Item> found = cur.getItems().stream()
                    .filter(i -> i.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (found.isEmpty()) throw new InvalidCommandException("В комнате нет предмета: " + name);
            Item it = found.get();
            cur.getItems().remove(it);
            ctx.getPlayer().getInventory().add(it);
            System.out.println("Взято: " + it.getName());
        });

        commands.put("inventory", (ctx, a) -> {
            var inv = ctx.getPlayer().getInventory();
            if (inv.isEmpty()) {
                System.out.println("Инвентарь пуст.");
                return;
            }
            // Упрощенная группировка по типу и названию предмета
            inv.stream()
                    .collect(Collectors.groupingBy(
                            i -> i.getClass().getSimpleName() + ":" + i.getName(),
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String[] parts = entry.getKey().split(":");
                        System.out.println("- " + parts[0] + " (" + entry.getValue() + "): " + parts[1]);
                    });
        });

        commands.put("use", (ctx, a) -> {
            if (a.isEmpty()) throw new InvalidCommandException("use: укажите имя предмета");
            String name = String.join(" ", a).trim();
            Player p = ctx.getPlayer();
            Optional<Item> found = p.getInventory().stream()
                    .filter(i -> i.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (found.isEmpty()) throw new InvalidCommandException("В инвентаре нет предмета: " + name);
            Item it = found.get();

            // Специальная логика для ключей - проверка дверей в текущей комнате
            if (it instanceof Key) {
                Room currentRoom = ctx.getCurrent();
                boolean doorUnlocked = false;

                for (Map.Entry<String, Room> entry : currentRoom.getNeighbors().entrySet()) {
                    Room neighbor = entry.getValue();
                    String direction = entry.getKey();

                    if (neighbor.isLocked() && neighbor.getRequiredKey() != null &&
                            neighbor.getRequiredKey().equals(it.getName())) {
                        System.out.println("🔑 Вы использовали '" + it.getName() + "', чтобы открыть дверь в Сокровищницу в направлении " + direction + "...");
                        neighbor.setLocked(false);
                        System.out.println("🔓 Дверь в Сокровищницу теперь открыта!");
                        p.getInventory().remove(it); // Ключ исчезает после использования
                        doorUnlocked = true;
                        break;
                    }
                }

                if (!doorUnlocked) {
                    System.out.println("Ключ не подходит ни к одной двери в этой комнате.");
                }
            } else {
                // Вызов полиморфного apply для других предметов
                it.apply(ctx);
            }
        });

        commands.put("fight", (ctx, a) -> {
            Room r = ctx.getCurrent();
            Monster m = r.getMonster();
            if (m == null) throw new InvalidCommandException("В комнате нет монстра для боя.");

            Player p = ctx.getPlayer();
            System.out.println("Начинается бой с: " + m.getName() + " (ур. " + m.getLevel() + ", HP: " + m.getHp() + ")");

            // Лог боя в файл (демонстрация try-with-resources)
            Path flog = Paths.get("fightlog.txt");
            try (BufferedWriter w = Files.newBufferedWriter(flog,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND)) {

                w.write("=== Fight started with " + m.getName() + " ===\n");

                while (m.getHp() > 0 && p.getHp() > 0) {
                    // Предложение действий
                    System.out.print("Введите 'fight' для атаки или 'run' для бегства: ");
                    String action;
                    try {
                        action = inputReader.readLine().trim().toLowerCase();
                    } catch (IOException e) {
                        throw new InvalidCommandException("Ошибка чтения ввода: " + e.getMessage());
                    }

                    if ("run".equals(action)) {
                        System.out.println("Вы сбежали из боя!");
                        w.write("Игрок сбежал из боя\n");
                        return;
                    }

                    if (!"fight".equals(action)) {
                        System.out.println("Неизвестное действие, продолжаем атаку");
                        action = "fight";
                    }

                    // Игрок бьёт
                    int dmgToMonster = Math.max(0, p.getAttack());
                    m.setHp(m.getHp() - dmgToMonster);
                    String s1 = "Вы бьёте " + m.getName() + "а на " + dmgToMonster + ". HP монстра: " + Math.max(0, m.getHp());
                    System.out.println(s1);
                    w.write(s1 + "\n");

                    if (m.getHp() <= 0) {
                        String defeated = "Монстр побеждён: " + m.getName();
                        System.out.println(defeated);
                        w.write(defeated + "\n");

                        // дроп лута
                        Potion loot = new Potion("Зелье", 3);
                        p.getInventory().add(loot);
                        String lootMsg = "Выпало: " + loot.getName();
                        System.out.println(lootMsg);
                        w.write(lootMsg + "\n");

                        // убрать монстра из комнаты
                        r.setMonster(null);
                        // начислим очки
                        ctx.addScore(10);
                        break;
                    }

                    // Монстр отвечает (уровень монстра как урон)
                    int dmgToPlayer = Math.max(0, m.getLevel());
                    p.setHp(p.getHp() - dmgToPlayer);
                    String s2 = "Монстр атакует вас на " + dmgToPlayer + " НР. Ваше HP сейчас: " + Math.max(0, p.getHp());
                    System.out.println(s2);
                    w.write(s2 + "\n");

                    if (p.getHp() <= 0) {
                        String dead = "Вы погибли. Игра окончена.";
                        System.out.println(dead);
                        w.write(dead + "\n");
                        // записать результат до выхода
                        SaveLoad.save(ctx);
                        // Завершить приложение
                        System.exit(0);
                    }

                }
            } catch (IOException e) {
                throw new UncheckedIOException("Ошибка записи лога боя", e);
            }
        });

        // Для демонстрации работы GC
        commands.put("alloc", (ctx, a) -> {
            System.out.println("🧹 Демонстрация работы Garbage Collector...");

            // Состояние памяти до
            Runtime rt = Runtime.getRuntime();
            long memoryBefore = rt.totalMemory() - rt.freeMemory();

            // Создание временных объектов
            List<String> tempObjects = new ArrayList<>();
            for (int i = 0; i < 100000; i++) {
                tempObjects.add("Временный объект " + i + " " + UUID.randomUUID() + " создан в " + new Date());
            }

            long memoryAfterAlloc = rt.totalMemory() - rt.freeMemory();
            System.out.println("Создано объектов: " + tempObjects.size());
            System.out.println("Память до: " + memoryBefore + " байт");
            System.out.println("Память после выделения: " + memoryAfterAlloc + " байт");

            // Освобождение ссылок
            tempObjects = null;

            // Принудительный вызов GC
            System.gc();
            try { Thread.sleep(1000); } catch (InterruptedException e) {} // Даем время GC

            long memoryAfterGC = rt.totalMemory() - rt.freeMemory();
            System.out.println("Память после GC: " + memoryAfterGC + " байт");
            System.out.println("Освобождено: " + (memoryAfterAlloc - memoryAfterGC) + " байт");
        });

        commands.put("save", (ctx, a) -> SaveLoad.save(ctx));
        commands.put("load", (ctx, a) -> SaveLoad.load(ctx));
        commands.put("scores", (ctx, a) -> SaveLoad.printScores());

        commands.put("debug", (ctx, a) -> {
            System.out.println("=== ДЕБАГ ИНФОРМАЦИЯ ===");
            System.out.println("Игрок: " + ctx.getPlayer().getName() +
                    " HP: " + ctx.getPlayer().getHp() +
                    " Атака: " + ctx.getPlayer().getAttack());
            System.out.println("Очки: " + ctx.getScore());
            System.out.println("Текущая комната: " + ctx.getCurrent().getName());
            System.out.println("Инвентарь: " + ctx.getPlayer().getInventory().size() + " предметов");

            // Дополнительно: вывод содержимого инвентаря
            if (!ctx.getPlayer().getInventory().isEmpty()) {
                System.out.println("Содержимое инвентаря:");
                ctx.getPlayer().getInventory().forEach(item ->
                        System.out.println("  - " + item.getName() + " (" + item.getClass().getSimpleName() + ")"));
            }
        });

        commands.put("about", (ctx, a) -> {
            System.out.println("DungeonMini — учебная консольная RPG игра");
            System.out.println("Реализовано: перемещение, инвентарь, бои, сохранение");
            System.out.println("Использует: Stream API, полиморфизм, try-with-resources");
        });

        commands.put("exit", (ctx, a) -> {
            System.out.println("Сохранение результатов...");
            SaveLoad.save(ctx);
            System.out.println("Пока!");
            System.exit(0);
        });
    }

    private void bootstrapWorld() {
        Player hero = new Player("Герой", 20, 5);
        state.setPlayer(hero);

        Room square = new Room("Площадь", "Каменная площадь с фонтаном.");
        Room forest = new Room("Лес", "Шелест листвы и птичий щебет.");
        Room cave = new Room("Пещера", "Темно и сыро. На стене висит старый ключ.");
        Room treasure = new Room("Сокровищница", "Комната полна золота и драгоценностей!");

        // Настройка связей между комнатами
        square.getNeighbors().put("north", forest);
        forest.getNeighbors().put("south", square);
        forest.getNeighbors().put("east", cave);
        cave.getNeighbors().put("west", forest);

        // Запертая дверь в сокровищницу (требует ключ)
        cave.getNeighbors().put("north", treasure);
        treasure.getNeighbors().put("south", cave);
        treasure.setLocked(true);
        treasure.setRequiredKey("Старый железный ключ");

        // Добавление предметов
        forest.getItems().add(new Potion("Малое зелье", 5));
        cave.getItems().add(new Key("Старый железный ключ"));
        treasure.getItems().add(new Weapon("Золотой меч", 5));
        treasure.getItems().add(new Potion("Большое зелье", 10));

        // Добавление монстров
        forest.setMonster(new Monster("Волк", 1, 8));
        cave.setMonster(new Monster("Гоблин", 2, 12));
        treasure.setMonster(new Monster("Дракон", 5, 30));

        // Регистрация комнат для сериализации
        SaveLoad.registerRoom(square);
        SaveLoad.registerRoom(forest);
        SaveLoad.registerRoom(cave);
        SaveLoad.registerRoom(treasure);

        state.setCurrent(square);
    }

    public void run() {
        System.out.println("DungeonMini. 'help' — команды.");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            this.inputReader = in; // Сохраняем для использования в fight

            while (true) {
                System.out.print("> ");
                String line = in.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;

                List<String> parts = Arrays.asList(line.split("\\s+"));
                String cmd = parts.get(0).toLowerCase(Locale.ROOT);
                List<String> args = parts.size() > 1 ? parts.subList(1, parts.size()) : Collections.emptyList();

                Command command = commands.get(cmd);
                try {
                    if (command == null) throw new InvalidCommandException("Неизвестная команда: " + cmd);
                    command.execute(state, args);
                    state.addScore(1); // Начисляем очки за каждую команду

                } catch (InvalidCommandException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace(); // Для отладки
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода: " + e.getMessage());
        }
    }

    /*
     * РАЗЛИЧИЕ ОШИБОК КОМПИЛЯЦИИ И ВЫПОЛНЕНИЯ:
     *
     * 1) ОШИБКИ КОМПИЛЯЦИИ - обнаруживаются при компиляции кода:
     *
     *
     * // Пример 1: Несовместимые типы
     * // int number = "строка"; // error: incompatible types: String cannot be converted to int
     *
     * // Пример 2: Необъявленная переменная
     * // unknownVariable = 5; // error: cannot find symbol
     *
     * // Пример 3: Отсутствующий импорт
     * // new NonExistentClass(); // error: cannot find symbol
     *
     * // Пример 4: Синтаксическая ошибка
     * // System.out.println("Hello world"  // error: ')' expected
     *
     * 2) ОШИБКИ ВЫПОЛНЕНИЯ (Runtime exceptions) - возникают во время работы программы:
     *    (этот код скомпилируется, но вызовет исключение при выполнении)
     *
     * // Пример 1: ArithmeticException - деление на ноль
     * // int result = 10 / 0; // ArithmeticException: / by zero
     *
     * // Пример 2: NullPointerException - обращение к null
     * // String str = null;
     * // int length = str.length(); // NullPointerException
     *
     * // Пример 3: ArrayIndexOutOfBoundsException - выход за границы массива
     * // int[] arr = new int[5];
     * // int value = arr[10]; // ArrayIndexOutOfBoundsException: Index 10 out of bounds for length 5
     *
     * // Пример 4: ClassCastException - неправильное приведение типов
     * // Object obj = "строка";
     * // Integer num = (Integer) obj; // ClassCastException: java.lang.String cannot be cast to java.lang.Integer
     *
     * // Пример 5: NumberFormatException - неправильный формат числа
     * // int num = Integer.parseInt("abc"); // NumberFormatException: For input string: "abc"
     *
     * 3) ОБРАБОТКА ИСКЛЮЧЕНИЙ В ЭТОМ ПРОЕКТЕ:
     *
     * Все некорректные ситуации в игре обрабатываются через InvalidCommandException:
     * - Неверные команды: throw new InvalidCommandException("Неизвестная команда: " + cmd)
     * - Неверные направления: throw new InvalidCommandException("Нельзя идти на" + dir + " отсюда.")
     * - Отсутствующие предметы: throw new InvalidCommandException("В комнате нет предмета: " + name)
     *
     * Эти исключения перехватываются в основном цикле и выводятся с префиксом "Ошибка: "
     */
}
