package rozprochy.rok2011.lab1.zad3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CLIChatClient implements ChatClient {

    private Connection connection;
    private String login;
    
    public static final int MAX_LOGIN = 6;
    public static final int MAX_MESSAGE = 20;
    
    
    public CLIChatClient(String login) throws LoginTooLongException {
        if (Utils.encode(login).length > MAX_LOGIN) {
            throw new LoginTooLongException();
        }
        this.login = login;
    }


    @Override
    public void gotDatagram(ChatDatagram data) {
        String sender = data.getUser();
        String text = data.getMessage();
        
        DateFormat fmt = new SimpleDateFormat("hh:mm:ss");
        String time = fmt.format(data.getSendTime());
        
        if (! sender.equals(login)) {
            System.out.printf("[%s] %s: %s\n", time, sender, text);
        }
    }
    
    
    /**
     * Reads the input and pushes the messages through the connection
     */
    public void inputLoop() {
        try {
            connection.run();
            
            BufferedReader stdin = new BufferedReader(
                    new InputStreamReader(System.in));
            
            String line;
            while ((line = stdin.readLine()) != null) {
                try {
                    Date time = new Date();
                    ChatDatagram data = new ChatDatagram(login, line, time);
                    connection.send(data);
                } catch (MessageTooLongException e) {
                    System.err.println("Message too long");
                } 
            }
            connection.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        
    }


    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
}
