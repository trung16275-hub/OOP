
// File: src/boss/Boss.java (Class gốc - dùng chung)
public abstract class Boss {
    public String name;
    public int hp;

    public Boss(String name, int hp) {
        this.name = name;
        this.hp = hp;
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
    }

    // Mỗi người sẽ tự viết logic tấn công riêng cho Boss của mình
    public abstract void updateBehavior(); 
}