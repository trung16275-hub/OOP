package model;

// File: src/player/Player.java
public class Player extends Combatant {
    // Chỉnh các chỉ số mặc định của Player tại đây.
    private static final String DEFAULT_NAME = "Hero";
    private static final int DEFAULT_MAX_HP = 100;
    private static final int DEFAULT_SPEED = 10;
    private static final int DEFAULT_ARMOR = 30;
    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 50;
    private static final double DEFAULT_HITBOX_RADIUS = 50.0;
    private static final double DEFAULT_DETECTION_RANGE = 250.0;

    private int armor = DEFAULT_ARMOR;
    private double detectionRange = DEFAULT_DETECTION_RANGE;
    private Weapon currentWeapon;
    
    //default constructor for testing
    public Player() {
        this(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_HITBOX_RADIUS);
    }

    public Player (String name, int maxHp, int speed) {
        this(name, maxHp, speed, DEFAULT_HITBOX_RADIUS);
    }

    public Player(String name, int maxHp, int speed, double hitboxRadius) {
        super(name, maxHp, speed, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }
    public int getArmor ( )
    {
        return armor ; 
    }

    public double getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(double detectionRange) {
        if (!Double.isFinite(detectionRange) || detectionRange < 0) {
            throw new IllegalArgumentException("Detection range must be a finite, non-negative value.");
        }
        this.detectionRange = detectionRange;
    }

    public boolean canDetect(Enemy enemy) {
        if (enemy == null || enemy.isDead() || isDead()) {
            return false;
        }

        double dx = (enemy.getX() + enemy.getWidth() / 2.0)
                  - (getX() + getWidth() / 2.0);
        double dy = (enemy.getY() + enemy.getHeight() / 2.0)
                  - (getY() + getHeight() / 2.0);

        return dx * dx + dy * dy <= detectionRange * detectionRange;
    }

    public void equip(Weapon w) {
        this.currentWeapon = w;
        System.out.println("Đã trang bị: " + w.name);
    }

    @Override
    public void attack(Combatant target) {
        if (currentWeapon != null && target !=null) {
            target.takeDamage(currentWeapon.damage);
        }
    }
    @Override 
    public void takeDamage ( int dmg )
    {
        if ( dmg <= 0 || isDead ( ) )
        {
            return ; 
        }
        int absorbed = Math.min ( armor , dmg ) ; 
        armor -= absorbed ; 
        super.takeDamage ( dmg - absorbed ) ;
    }
    
}
