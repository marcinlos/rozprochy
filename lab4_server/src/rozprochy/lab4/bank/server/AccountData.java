package rozprochy.lab4.bank.server;

import rozprochy.lab4.generic.BasicAccountData;

public class AccountData extends BasicAccountData{
    
    private int amount;
    
    public AccountData(String owner, int amount, byte[] hashed, byte[] salt) {
        super(owner, hashed, salt);
        this.amount = amount;
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