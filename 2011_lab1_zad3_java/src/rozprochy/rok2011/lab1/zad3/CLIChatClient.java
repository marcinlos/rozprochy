package rozprochy.rok2011.lab1.zad3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class CLIChatClient implements ChatClient {

    private Connection connection;
    private String login;
    
    
    public CLIChatClient(String login) {
        this.login = login;
    }


    @Override
    public void gotDatagram(ChatDatagram data) {
        String sender = data.getUser();
        String text = data.getMessage();
        
        if (! sender.equals(login)) {
            System.out.println(sender + ": " + text);
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
                ChatDatagram data = new ChatDatagram(login, line);
                connection.send(data);
            }
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
