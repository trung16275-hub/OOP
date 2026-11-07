package model;

//Class cho cả Player và Boss/Enemy

public abstract class Combatant {
    private String name;
    private int hp;
    private int maxHp;

    // Tọa độ và kích thước trong không gian game
    protected double x;
    protected double y;
    protected int width;
    protected int height;

    public Combatant (String name, int maxHp) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(maxHp, hp));
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        this.hp = Math.min(this.hp, maxHp);
    }

    public int takeDamage(int dmg) {
        if(dmg <= 0) return 0;
        int actualHpLoss= Math.min(hp, dmg);
        hp -= actualHpLoss;
        return actualHpLoss;
    }

    public boolean isDead() {
        return hp<=0;
    }

    public abstract void attack(Combatant target);



}
