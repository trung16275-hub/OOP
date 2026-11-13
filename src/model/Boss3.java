package model;

public class Boss3 extends Enemy {
    public Boss3() {
        this(30);
    }

    public Boss3(double hitboxRadius) {
        super("Boss 3", 200, 3, 2, Double.POSITIVE_INFINITY, hitboxRadius);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 3
        System.out.println(this.getName() + " đang tấn công!");
    }
}
