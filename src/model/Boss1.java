package model;

public class Boss1 extends Enemy {
    public Boss1() {
        super("Boss 1", 100,0,30,Double.POSITIVE_INFINITY) ;
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 1
        System.out.println(this.getName() + " đang tấn công!");
    }
}
