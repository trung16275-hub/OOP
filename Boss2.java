package OOP;

public class Boss2 extends Boss {
    public Boss2() {
        super("Boss 2", 150);
    }

@Override
    public void updateBehavior() {
        // Logic tấn công của Boss 2
        System.out.println(name + " đang tấn công!");
    }
}
