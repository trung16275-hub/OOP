package model;

public class Boss1 extends Boss {
    public Boss1() {
        super("Boss 1", 100);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 1
        System.out.println(name + " đang tấn công!");
    }
}
