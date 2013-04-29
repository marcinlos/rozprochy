package rozprochy.lab4.generic;


public class BasicAccountData implements AccountData {

    private String login;
    private byte[] hashedPassword;
    private byte[] salt;
    
    public BasicAccountData(String login, byte[] hashedPassword, byte[] salt) {
        this.login = login;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }
    
    @Override
    public String getLogin() {
        return login;
    }
    
    @Override
    public byte[] getHashedPassword() {
        return hashedPassword;
    }
    
    @Override
    public byte[] getSalt() {
        return salt;
    }

}
