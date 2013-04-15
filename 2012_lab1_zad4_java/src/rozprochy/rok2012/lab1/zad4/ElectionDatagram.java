package rozprochy.rok2012.lab1.zad4;

import java.nio.ByteBuffer;

import rozprochy.rok2012.lab1.zad4.connection.Datagram;

public class ElectionDatagram implements Datagram {
    
    private long id;
    private String login;

    public ElectionDatagram(long id, String login) {
        this.id = id;
        this.login = login;
    }
    
    public long getId() {
        return id;
    }
    
    public String getLogin() {
        return login;
    }

    @Override
    public void marshall(ByteBuffer buffer) {
        byte[] utfLogin = Utils.encode(login);
        buffer.put((byte) utfLogin.length);
        buffer.putLong(id);
        buffer.put(utfLogin);
    }
    
    
    public static ElectionDatagram decode(ByteBuffer buffer) {
        byte len = buffer.get();
        byte[] utfLogin = new byte[len];
        long id = buffer.getLong();
        buffer.get(utfLogin);
        String login = Utils.decode(utfLogin);
        return new ElectionDatagram(id, login);
    }

}
