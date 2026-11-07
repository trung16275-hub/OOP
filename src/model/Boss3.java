package model;

public class Boss3 extends Boss {
    public Boss3() {
        super("Boss 3", 200);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 3
        System.out.println(name + " đang tấn công!");
    }
}
