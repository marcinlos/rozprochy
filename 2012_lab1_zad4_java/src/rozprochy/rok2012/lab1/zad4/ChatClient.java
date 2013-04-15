package rozprochy.rok2012.lab1.zad4;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import rozprochy.rok2012.lab1.zad4.cli.CommandHandler;
import rozprochy.rok2012.lab1.zad4.connection.Connection;
import rozprochy.rok2012.lab1.zad4.connection.DatagramHandler;
import rozprochy.rok2012.lab1.zad4.connection.Parser;



public class ChatClient implements DatagramHandler<ChatDatagram>,
    CommandHandler {

    private Connection connection;
    private String login;
    
    public static final int MAX_LOGIN = 6;
    public static final int MAX_MESSAGE = 20;
    
    
    public ChatClient(String login) throws LoginTooLongException {
        if (login.length() > MAX_LOGIN) {
            throw new LoginTooLongException();
        }
        this.login = login;
    }

    @Override
    public void handle(byte type, ChatDatagram data) {
        String sender = data.getUser();
        String text = data.getMessage();
        
        DateFormat fmt = new SimpleDateFormat("hh:mm:ss");
        String time = fmt.format(data.getSendTime());
        
        if (! sender.equals(login)) {
            System.out.printf("[%s] %s: %s\n", time, sender, text);
        }
    }
    
    @Override
    public boolean handle(String line) {
        if (line.length() > MAX_MESSAGE) {
            System.err.println("Message too long");
        } else {
            try {
                Date time = new Date();
                ChatDatagram data = new ChatDatagram(login, line, time);
                connection.send(DatagramType.MESSAGE, data);
            } catch (RuntimeException e) {
                e.printStackTrace(System.err);
                return false;
            }
        }
        return true;
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public static Parser<ChatDatagram> getParser() {
        return new Parser<ChatDatagram>() {
            @Override
            public ChatDatagram parse(byte type, ByteBuffer buffer) {
                return ChatDatagram.unmarshall(buffer);
            }
        };
    }

}
