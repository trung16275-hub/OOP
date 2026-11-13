package model;

public class Boss2 extends Enemy {
    public Boss2() {
        this(30);
    }

    public Boss2(double hitboxRadius) {
        super("Boss 2", 150, 2, 2, 10, hitboxRadius);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 2
        System.out.println(this.getName() + " đang tấn công!");
    }
}
