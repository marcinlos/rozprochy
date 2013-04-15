package rozprochy.rok2012.lab1.zad4;

import java.nio.ByteBuffer;
import java.util.Date;

import rozprochy.rok2012.lab1.zad4.connection.Datagram;


/**
 * Class representing data unit of chat protocol.
 * 
 * PDU has a simple format:
 * 
 * +-------+
 * | TYPE  |
 * +-------+-------+-------+-------+-------+-------+
 * | L_LEN | M_LEN |          CONTROL_SUM          |
 * +-------+-------+-------+-------+-----.....-----+
 * |           SEND_TIME           |     LOGIN     |     
 * +-------+-------+-----.....-----+-----.....-----+
 * |                    MESSAGE                    |
 * +-------+-------+-----.....-----+-------+-------+
 * 
 * TYPE          8 bits    message type (1)
 * L_LEN         8 bits    length of sender's login
 * M_LEN         8 bits    length of message content
 * CHECKSUM     32 bits    computed from send time, login and content
 * SEND_TIME    32 bits    amount of seconds in UNIX era
 * LOGIN        variable   utf-8-encoded sender login
 * MESSAGE      variable   utf-8-encoded message content
 *     
 */
public class ChatDatagram implements Datagram {
    
    /** Sender login */
    private String user;
    
    /** Content of the message */
    private String message;
    
    /** Control sum */
    private int checkSum;
    
    /** Time the message was sent */
    private Date sendTime;
    
    public ChatDatagram(String user, String message, Date time) {
        this.user = user;
        this.message = message;
        this.sendTime = time;
        this.checkSum = computeCheckSum();
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }
    
    public Date getSendTime() {
        return sendTime;
    }
    
    public int getSendSeconds() {
        return (int) (sendTime.getTime() / 1000);
    }
    
    public int getCheckSum() {
        return checkSum;
    }
    
    public int computeCheckSum() {
        Encoder e = new Encoder();
        e.addData(Utils.encode(user));
        e.addData(Utils.encode(message));
        int cs = e.getValue();
        cs ^= getSendSeconds();
        return cs;
    }
    
    @Override
    public void marshall(ByteBuffer buffer) {
        byte[] utfUser = Utils.encode(user);
        byte[] utfMessage = Utils.encode(message);

        //int totalSize = 10 + utfUser.length + utfMessage.length;
        
        // Write unsigned length 
        buffer.put((byte) utfUser.length);
        buffer.put((byte) utfMessage.length);
        buffer.putInt(checkSum);
        buffer.putInt(getSendSeconds());
        buffer.put(utfUser);
        buffer.put(utfMessage);
    }
    
    
    public static ChatDatagram unmarshall(ByteBuffer buffer) {
        
        // Read it as an unsigned value
        int lenUser = buffer.get();
        int lenMessage = buffer.get();
        
        int checksum = buffer.getInt();
        long t = buffer.getInt();
        Date time = new Date(t * 1000);
        
        byte[] utfUser = new byte[lenUser];
        byte[] utfMessage = new byte[lenMessage];
        
        buffer.get(utfUser);
        buffer.get(utfMessage);
        
        String user = Utils.decode(utfUser);
        String message = Utils.decode(utfMessage);
        
        ChatDatagram datagram = new ChatDatagram(user, message, time);
        if (datagram.getCheckSum() != checksum) {
            throw new IllegalStateException("Invalid checksum");
        }
        return datagram;
    }
    
    
    private static class Encoder {
        private int value = 0;
        
        public void addData(byte[] bytes) {
            for (byte b: bytes) {
                value *= 17;
                value += b;
            }
        }
        
        public int getValue() {
            return value;
        }
    }
    
}
