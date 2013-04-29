package rozprochy.lab4.generic;

import java.io.Serializable;

public interface AccountData extends Serializable {
    
    String getLogin();
    byte[] getHashedPassword();
    byte[] getSalt();

}
