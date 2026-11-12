package model;

public class Boss2 extends Enemy {
    public Boss2() {
        super("Boss 2", 150,2,2,Double.POSITIVE_INFINITY);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 2
        System.out.println(this.getName() + " đang tấn công!");
    }
}
