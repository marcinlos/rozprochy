package rozprochy.lab4.bank.server;

public class AccountData {
    
    private String owner;
    private int amount;
    
    public AccountData(String owner, int amount) {
        this.owner = owner;
        this.amount = amount;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getAmount() {
        return amount;
    }

    public void deposit(int amount) {
        this.amount += amount;
    }
    
    public void withdraw(int amount) {
        this.amount -= amount;
    }
    
}