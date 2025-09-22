package com.example.dungeon.model;

public class Monster extends Entity {
    private int level;
    private final int stHp;

    public Monster(String name, int level, int hp) {
        super(name, hp);
        this.level = level;
        stHp = hp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStHP() { return stHp; }
}
