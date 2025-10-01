package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SaveLoad {
    private static final Path SAVE = Paths.get("save.txt");
    private static final Path SCORES = Paths.get("scores.csv");
    private static final Map<String, Room> roomRegistry = new HashMap<>();

    // Регистрируем комнату для сериализации
    public static void registerRoom(Room room) {
        roomRegistry.put(room.getName(), room);
    }

    // Получить комнату по имени
    public static Room getRoom(String name) {
        return roomRegistry.get(name);
    }

    public static void save(GameState s) {
        try (BufferedWriter w = Files.newBufferedWriter(SAVE)) {
            Player p = s.getPlayer();

            // Сохраняем данные игрока
            w.write("player;" + p.getName() + ";" + p.getHp() + ";" + p.getAttack());
            w.newLine();

            // Сохраняем инвентарь
            String inv = p.getInventory().stream()
                    .map(i -> i.getClass().getSimpleName() + ":" + i.getName())
                    .collect(Collectors.joining(","));
            w.write("inventory;" + inv);
            w.newLine();

            // Сохраняем текущую комнату
            w.write("room;" + s.getCurrent().getName());
            w.newLine();

            // Сохраняем состояние всех комнат
            w.write("rooms;");
            String roomsData = roomRegistry.values().stream()
                    .map(room -> {
                        String monsterData = "null";
                        if (room.getMonster() != null) {
                            Monster m = room.getMonster();
                            monsterData = m.getName() + "," + m.getHp() + "," + m.getLevel();
                        }

                        String itemsData = room.getItems().stream()
                                .map(item -> item.getClass().getSimpleName() + "=" + item.getName())
                                .collect(Collectors.joining("|"));

                        return room.getName() + ":" +
                                room.isLocked() + ":" +
                                (room.getRequiredKey() != null ? room.getRequiredKey() : "null") + ":" +
                                monsterData + ":" +
                                itemsData;
                    })
                    .collect(Collectors.joining(";"));
            w.write(roomsData);
            w.newLine();

            // Сохраняем счёт
            w.write("score;" + s.getScore());
            w.newLine();

            System.out.println("Сохранено в " + SAVE.toAbsolutePath());
            writeScore(p.getName(), s.getScore());

        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить игру", e);
        }
    }

    public static void load(GameState s) {
        if (!Files.exists(SAVE)) {
            System.out.println("Сохранение не найдено.");
            return;
        }

        try (BufferedReader r = Files.newBufferedReader(SAVE)) {
            Map<String, String> map = new HashMap<>();
            for (String line; (line = r.readLine()) != null; ) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }

            // Загрузка игрока
            Player p = s.getPlayer();
            if (map.containsKey("player")) {
                String[] playerData = map.get("player").split(";");
                if (playerData.length >= 4) {
                    p.setName(playerData[0]);
                    p.setHp(Integer.parseInt(playerData[1]));
                    p.setAttack(Integer.parseInt(playerData[2]));
                }
            }

            // Очищаем инвентарь перед загрузкой
            p.getInventory().clear();

            // Загрузка инвентаря
            if (map.containsKey("inventory")) {
                String invData = map.get("inventory");
                if (!invData.isBlank()) {
                    for (String itemToken : invData.split(",")) {
                        String[] itemParts = itemToken.split(":", 2);
                        if (itemParts.length == 2) {
                            switch (itemParts[0]) {
                                case "Potion" -> p.getInventory().add(new Potion(itemParts[1], 5));
                                case "Key" -> p.getInventory().add(new Key(itemParts[1]));
                                case "Weapon" -> p.getInventory().add(new Weapon(itemParts[1], 3));
                            }
                        }
                    }
                }
            }

            // Загрузка текущей комнаты
            if (map.containsKey("room")) {
                Room currentRoom = roomRegistry.get(map.get("room"));
                if (currentRoom != null) {
                    s.setCurrent(currentRoom);
                }
            }

            // Загрузка состояния комнат
            if (map.containsKey("rooms")) {
                String[] roomsData = map.get("rooms").split(";");
                for (String roomData : roomsData) {
                    String[] roomParts = roomData.split(":", 5);
                    if (roomParts.length >= 5) {
                        Room room = roomRegistry.get(roomParts[0]);
                        if (room != null) {
                            // Загрузка состояния двери
                            room.setLocked(Boolean.parseBoolean(roomParts[1]));
                            if (!"null".equals(roomParts[2])) {
                                room.setRequiredKey(roomParts[2]);
                            }

                            // Загрузка монстра
                            if (!"null".equals(roomParts[3])) {
                                String[] monsterData = roomParts[3].split(",");
                                if (monsterData.length >= 3) {
                                    Monster monster = new Monster(
                                            monsterData[0],
                                            Integer.parseInt(monsterData[2]),
                                            Integer.parseInt(monsterData[1])
                                    );
                                    room.setMonster(monster);
                                }
                            } else {
                                room.setMonster(null);
                            }

                            // Загрузка предметов
                            room.getItems().clear();
                            if (!roomParts[4].isEmpty()) {
                                String[] itemsData = roomParts[4].split("\\|");
                                for (String itemData : itemsData) {
                                    String[] itemParts = itemData.split("=", 2);
                                    if (itemParts.length == 2) {
                                        switch (itemParts[0]) {
                                            case "Potion" -> room.getItems().add(new Potion(itemParts[1], 5));
                                            case "Key" -> room.getItems().add(new Key(itemParts[1]));
                                            case "Weapon" -> room.getItems().add(new Weapon(itemParts[1], 3));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Загрузка счёта
            if (map.containsKey("score")) {
                s.addScore(Integer.parseInt(map.get("score")));
            }

            System.out.println("Игра загружена полностью! Восстановлено состояние всех комнат.");

        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось загрузить игру", e);
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке: " + e.getMessage());
        }
    }

    public static void printScores() {
        if (!Files.exists(SCORES)) {
            System.out.println("Пока нет результатов.");
            return;
        }

        try (BufferedReader r = Files.newBufferedReader(SCORES)) {
            System.out.println("ТАБЛИЦА ЛИДЕРОВ (ТОП-10)");
            System.out.println("┌───────┬──────────────────┬──────────────┬───────┐");
            System.out.println("│ Место │ Игрок            │ Дата         │ Очки  │");
            System.out.println("├───────┼──────────────────┼──────────────┼───────┤");

            List<ScoreRecord> scores = r.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length >= 3)
                    .map(parts -> new ScoreRecord(
                            parts[0], // timestamp
                            parts[1], // player name
                            Integer.parseInt(parts[2]) // score
                    ))
                    .sorted(Comparator.comparingInt(ScoreRecord::score).reversed())
                    .limit(10)
                    .toList();

            if (scores.isEmpty()) {
                System.out.println("│       │ Нет данных       │              │       │");
            } else {
                for (int i = 0; i < scores.size(); i++) {
                    ScoreRecord record = scores.get(i);
                    String shortDate = record.timestamp().length() > 16 ?
                            record.timestamp().substring(0, 16) : record.timestamp();

                    System.out.printf("│ %-5d │ %-16s │ %-12s │ %-5d │\n",
                            i + 1,
                            record.player().length() > 16 ? record.player().substring(0, 16) : record.player(),
                            shortDate,
                            record.score());
                }
            }

            System.out.println("└───────┴──────────────────┴──────────────┴───────┘");

        } catch (IOException e) {
            System.err.println("Ошибка чтения результатов: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка обработки результатов: " + e.getMessage());
        }
    }

    private static void writeScore(String player, int score) {
        try {
            boolean header = !Files.exists(SCORES);
            try (BufferedWriter w = Files.newBufferedWriter(SCORES,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                if (header) {
                    w.write("timestamp,player,score");
                    w.newLine();
                }

                w.write(LocalDateTime.now() + "," + player + "," + score);
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Не удалось записать очки: " + e.getMessage());
        }
    }

    // Вспомогательная запись для хранения результатов
    private record ScoreRecord(String timestamp, String player, int score) {
    }
}