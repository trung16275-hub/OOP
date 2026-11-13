package model;

public class Boss1 extends Enemy {
    public Boss1() {
        this(200);
    }

    public Boss1(double hitboxRadius) {
        super("Boss 1", 100, 0, 10, 500, hitboxRadius);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 1
        System.out.println(this.getName() + " đang tấn công!");
    }
}
