package common;

public class Player {
    private String name;
    private int avatar;
    private int balance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public Player(String name, int avatar, int balance) {
        this.name = name;
        this.avatar = avatar;
        this.balance = balance;
    }

    // Getter & Setter...
}
