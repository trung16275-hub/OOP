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
    private double facingX;
    private double facingY;
    private double hitboxRadius = -1;

    public Combatant (String name, int maxHp, int speed) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = speed;
    }
    public Combatant(String name, int maxHp, int speed, double hitboxRadius) {
        this(name, maxHp, speed);
        setHitboxRadius(hitboxRadius);
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

    public double getFacingX() {
        return facingX;
    }

    public double getFacingY() {
        return facingY;
    }

    public double getHitboxRadius() {
        return hitboxRadius >= 0 ? hitboxRadius : Math.min(width, height) / 2.0;
    }

    public void setHitboxRadius(double hitboxRadius) {
        if (!Double.isFinite(hitboxRadius) || hitboxRadius < 0) {
            throw new IllegalArgumentException("Hitbox radius must be a finite, non-negative value.");
        }
        this.hitboxRadius = hitboxRadius;
    }

    public boolean isWithinHitbox(Combatant other) {
        if (other == null || other == this) {
            return false;
        }

        double dx = (other.getX() + other.getWidth() / 2.0)- (getX() + getWidth() / 2.0);
        double dy = (other.getY() + other.getHeight() / 2.0)- (getY() + getHeight() / 2.0);
        double radius = getHitboxRadius() + other.getHitboxRadius();

        return dx * dx + dy * dy <= radius * radius;
    }

    public void faceToward(Combatant other) {
        if (other == null || other == this) {
            return;
        }

        double dx = (other.getX() + other.getWidth() / 2.0)- (getX() + getWidth() / 2.0);
        double dy = (other.getY() + other.getHeight() / 2.0)- (getY() + getHeight() / 2.0);
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            facingX = dx / distance;
            facingY = dy / distance;
        }
    }

    public void resetFacing() {
        facingX = 0;
        facingY = 0;
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
