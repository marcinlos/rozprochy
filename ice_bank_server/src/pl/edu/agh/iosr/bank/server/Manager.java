package pl.edu.agh.iosr.bank.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Bank.IncorrectData;
import Bank.NoSuchAccount;
import Bank.PersonalData;
import Bank.RequestRejected;
import Bank._BankManagerDisp;
import Bank.accountType;
import Ice.Current;
import Ice.StringHolder;

public class Manager extends _BankManagerDisp {
    
    private static final Logger logger = LoggerFactory.getLogger(Manager.class);

    @Override
    public void createAccount(PersonalData data, accountType type,
            StringHolder accountID, Current __current) throws IncorrectData,
            RequestRejected {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAccount(String accountID, Current __current)
            throws IncorrectData, NoSuchAccount {
        // TODO Auto-generated method stub
        
    }


}
