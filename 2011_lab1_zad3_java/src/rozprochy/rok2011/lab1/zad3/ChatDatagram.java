package rozprochy.rok2011.lab1.zad3;

import java.nio.ByteBuffer;

/**
 * Class representing data unit of chat protocol.
 * 
 * PDU has a simple format:
 * 
 * +---+---+--+-...-+--+--+--+-...-+--+--+
 * |  LEN  |   LOGIN   |     CONTENT     |
 * +---+---+--+-...-+--+--+--+-...-+--+--+
 * 
 * LEN: 16 bits     length (in bytes) of sender's login 
 * LOGIN, CONTENT:  sender login and message content, as utf-8
 */
public class ChatDatagram {
    
    /** Sender login */
    private String user;
    
    /** Content of the message */
    private String message;
    
    
    public ChatDatagram(String user, String message) {
        this.user = user;
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }


    public byte[] marshall() {
        byte[] utfUser = Utils.encode(user);
        byte[] utfMessage = Utils.encode(message);

        // Kinda paranoic, but...
        if (utfUser.length >= (1 << 16)) {
            throw new IllegalStateException("User name too long");
        }
        
        int totalSize = 4 + utfUser.length + utfMessage.length;
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        
        // Write unsigned length 
        buffer.putShort((short) (utfUser.length & 0xffff));
        
        buffer.put(utfUser);
        buffer.put(utfMessage);
        return buffer.array();
    }
    
    
    public static ChatDatagram unmarshall(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        
        // Read it as an unsigned value
        int lenUser = buffer.getShort() & 0xffff;
        int lenMessage = bytes.length - lenUser - 4;
        
        byte[] utfUser = new byte[lenUser];
        byte[] utfMessage = new byte[lenMessage];
        
        buffer.get(utfUser);
        buffer.get(utfMessage);
        
        String user = Utils.decode(utfUser);
        String message = Utils.decode(utfMessage);
        
        return new ChatDatagram(user, message);
    }
    
}
