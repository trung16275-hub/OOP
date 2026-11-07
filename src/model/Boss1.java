package model;

public class Boss1 extends Enemy {
    public Boss1() {
        super("Boss 1", 100,2);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 1
        System.out.println(this.getName() + " đang tấn công!");
    }
}
