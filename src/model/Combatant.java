package model;

//Class cho cả Player và Boss/Enemy

import map.Room;

public abstract class Combatant {
    private String name;
    private int hp;
    private int maxHp;
    private int speed;

    // Tọa độ và kích thước trong không gian game
    protected double x;
    protected double y;
    protected int width;
    protected int height;

    public Combatant (String name, int maxHp, int speed) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
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

    public void takeDamage(int dmg) {
        if(dmg <= 0) return ;
        this.hp = Math.max(0, this.hp - dmg);
    }

    public boolean isDead() {
        return hp<=0;
    }

    public abstract void attack(Combatant target) ;
    //Cần fix
    public void clampPosition ( int ScreenWidth , int ScreenHeight ) {
        double maxX = Math.max ( 0 , ScreenWidth - this.width - Room.TILE_SIZE) ;
        double maxY = Math.max ( 0 , ScreenHeight - this.height -Room.TILE_SIZE) ;
        x = Math.max ( Room.TILE_SIZE , Math.min ( x , maxX )) ;
        y = Math.max ( Room.TILE_SIZE , Math.min ( y , maxY)) ;
    }

}
