package rozprochy.lab4.bank.server;

import java.io.Serializable;

public class AccountData implements Serializable {
    
    private String owner;
    private int amount;
    
    private byte[] hashed;
    private byte[] salt;
    
    public AccountData(String owner, int amount, byte[] hashed, byte[] salt) {
        this.owner = owner;
        this.amount = amount;
        this.hashed = hashed;
        this.salt = salt;
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
    
    public byte[] getHashed() {
        return hashed;
    }
    
    public byte[] getSalt() {
        return salt;
    }
    
}