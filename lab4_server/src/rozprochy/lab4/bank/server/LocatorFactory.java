package rozprochy.lab4.bank.server;

import java.util.Map;

import Ice.ServantLocator;

public class LocatorFactory {

    public static ServantLocator newInstance(String type,
            SessionManager sessions, AccountManager accounts, 
            Map<String, String> config) throws UnknownLocatorType {
        if ("Balanced".equals(type)) {
            return new RoundRobinLocator(sessions, accounts, config);
        } else if ("PerSession".equals(type)) {
            return new PerSessionLocator(sessions, accounts, config);
        } else {
            throw new UnknownLocatorType();
        }
    }

}
