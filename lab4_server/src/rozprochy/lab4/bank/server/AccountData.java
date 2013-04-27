package rozprochy.lab4.bank.server;

import java.io.Serializable;

public class AccountData implements Serializable {
    
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