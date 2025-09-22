package com.example.dungeon.model;

import java.util.*;
import java.util.stream.Collectors;

public class Room {
    private final String name;
    private final String description;
    private final Map<String, Room> neighbors = new HashMap<>();
    private final List<Item> items = new ArrayList<>();
    private Monster monster;

    // Поля для запертых дверей
    private boolean locked = false;
    private String requiredKey;

    public Room(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Room> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(String direction, Room room) {
        neighbors.put(direction, room);
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    // Методы для работы с замками
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getRequiredKey() {
        return requiredKey;
    }

    public void setRequiredKey(String requiredKey) {
        this.requiredKey = requiredKey;
    }

    // Проверка, можно ли открыть дверь с данным ключом
    public boolean canUnlockWith(String keyName) {
        return locked && requiredKey != null && requiredKey.equals(keyName);
    }

    // Открыть дверь (снять замок)
    public void unlock() {
        if (locked) {
            locked = false;
            System.out.println("🔓 Дверь открыта!");
        }
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("📍 ").append(name).append(": ").append(description);

        if (!items.isEmpty()) {
            sb.append("\n📦 Предметы: ").append(items.stream()
                    .map(Item::getName)
                    .collect(Collectors.joining(", ")));
        }

        if (monster != null) {
            sb.append("\n🐺 В комнате монстр: ").append(monster.getName())
                    .append(" (ур. ").append(monster.getLevel())
                    .append(", HP: ").append(monster.getHp()).append(")");
        }

        if (locked && requiredKey != null) {
            sb.append("\n🔒 Дверь заперта! Нужен ключ: ").append(requiredKey);
        }

        if (!neighbors.isEmpty()) {
            sb.append("\n🚪 Выходы: ").append(String.join(", ", neighbors.keySet()));
        }

        return sb.toString();
    }

    // Сериализация для сохранения состояния
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":")
                .append(description).append(":")
                .append(locked).append(":")
                .append(requiredKey != null ? requiredKey : "null").append(":");

        // Сериализация монстра
        if (monster != null) {
            sb.append(monster.getName()).append(",")
                    .append(monster.getHp()).append(",")
                    .append(monster.getLevel());
        } else {
            sb.append("null");
        }
        sb.append(":");

        // Сериализация предметов
        sb.append(items.stream()
                .map(item -> item.getClass().getSimpleName() + "=" + item.getName())
                .collect(Collectors.joining("|")));

        return sb.toString();
    }

    // Десериализация из строки
    public static Room deserialize(String data) {
        String[] parts = data.split(":", 6);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Неверный формат данных комнаты");
        }

        Room room = new Room(parts[0], parts[1]);
        room.setLocked(Boolean.parseBoolean(parts[2]));

        if (!"null".equals(parts[3])) {
            room.setRequiredKey(parts[3]);
        }

        // Восстановление монстра
        if (!"null".equals(parts[4])) {
            String[] monsterData = parts[4].split(",");
            if (monsterData.length == 3) {
                Monster monster = new Monster(
                        monsterData[0],
                        Integer.parseInt(monsterData[2]), // level
                        Integer.parseInt(monsterData[1])  // hp
                );
                room.setMonster(monster);
            }
        }

        // Восстановление предметов
        if (!parts[5].isEmpty()) {
            String[] itemsData = parts[5].split("\\|");
            for (String itemData : itemsData) {
                String[] itemParts = itemData.split("=", 2);
                if (itemParts.length == 2) {
                    switch (itemParts[0]) {
                        case "Potion" -> room.addItem(new Potion(itemParts[1], 5));
                        case "Key" -> room.addItem(new Key(itemParts[1]));
                        case "Weapon" -> room.addItem(new Weapon(itemParts[1], 3));
                    }
                }
            }
        }

        return room;
    }

    // Метод для клонирования комнаты
    public Room clone() {
        Room clone = new Room(name, description);
        clone.locked = this.locked;
        clone.requiredKey = this.requiredKey;

        if (this.monster != null) {
            clone.monster = new Monster(
                    this.monster.getName(),
                    this.monster.getLevel(),
                    this.monster.getHp()
            );
        }

        // Клонируем предметы
        for (Item item : this.items) {
            if (item instanceof Potion) {
                clone.items.add(new Potion(item.getName(), 5));
            } else if (item instanceof Key) {
                clone.items.add(new Key(item.getName()));
            } else if (item instanceof Weapon) {
                clone.items.add(new Weapon(item.getName(), 3));
            }
        }

        return clone;
    }

    @Override
    public String toString() {
        return name + " (" + description + ")";
    }

    // Метод для проверки наличия выхода в указанном направлении
    public boolean hasExit(String direction) {
        return neighbors.containsKey(direction);
    }

    // Метод для получения доступных направлений
    public List<String> getAvailableExits() {
        return new ArrayList<>(neighbors.keySet());
    }

    // Метод для проверки, есть ли в комнате предмет с указанным именем
    public boolean hasItem(String itemName) {
        return items.stream().anyMatch(item -> item.getName().equalsIgnoreCase(itemName));
    }

    // Метод для поиска предмета по имени
    public Optional<Item> findItem(String itemName) {
        return items.stream()
                .filter(item -> item.getName().equalsIgnoreCase(itemName))
                .findFirst();
    }
}