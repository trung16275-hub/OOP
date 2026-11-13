package model;

// File: src/player/Player.java
public class Player extends Combatant {
    private static final double DEFAULT_HITBOX_RADIUS = 50.0;
    private int armor = 30 ; 
    private Weapon currentWeapon;
    
    //default constructor for testing
    public Player() {
        this("Hero", 100, 10, DEFAULT_HITBOX_RADIUS);
    }

    public Player (String name, int maxHp, int speed) {
        this(name, maxHp, speed, DEFAULT_HITBOX_RADIUS);
    }

    public Player(String name, int maxHp, int speed, double hitboxRadius) {
        super(name, maxHp, speed, hitboxRadius);
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }
    public int getArmor ( )
    {
        return armor ; 
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
