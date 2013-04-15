package rozprochy.lab2.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Config {
    
    private String server;
    private int port;
    private String login;

    public Config(String[] args) {
        if (args.length < 1) {
            usageError();
        }
        if (args[0].equals("-f")) {
            // property file based configuration
            if (args.length < 2) {
                System.err.println("Filename must follow -f");
                System.exit(1);
            }
            loadFromFile(new File(args[1]));
        } else {
            if (args.length < 3) {
                usageError();
            }
            server = args[0];
            port = Integer.parseInt(args[1]);
            login = args[2];
        }
    }

    private void usageError() {
        System.err.println("Usage: client [-f <prop-file> | " + 
                "<server> <port> <login>]");
        System.exit(1);
    }
    
    private void loadFromFile(File file) {
        InputStream input = null;
        try {
            Properties props = new Properties();
            props.load(input);
            loadFromProperties(props);
        } catch (IOException e) {
            System.err.println("Error reading configuration");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
    
    private void loadFromProperties(Properties props) {
        this.server = props.getProperty("host", "localhost");
        this.port = Integer.parseInt(props.getProperty("port", "1099"));
        this.login = props.getProperty("login");
    }

    public String getHost() {
        return server;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getLogin() {
        return login;
    }

}
