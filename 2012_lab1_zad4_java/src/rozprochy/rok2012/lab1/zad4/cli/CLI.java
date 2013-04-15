package rozprochy.rok2012.lab1.zad4.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CLI {

    protected final BufferedReader input;

    private CommandHandler defaultHandler;

    private Map<String, CommandHandler> handlers = 
            new HashMap<String, CommandHandler>();
    

    public CLI() throws IOException {
        input = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public void setDefaultHandler(CommandHandler handler) {
        this.defaultHandler = handler;
    }
    
    public void addHandler(String command, CommandHandler handler) {
        handlers.put(command, handler);
    }

    public void run() throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (! interpret(line)) {
                break;
            }
        }
    }

    private boolean interpret(String line) {
        Scanner scanner = new Scanner(line);
        if (scanner.hasNext()) {
            String cmd = scanner.next();
            CommandHandler handler = handlers.get(cmd);
            String args = scanner.hasNext() ? scanner.nextLine() : null;
            if (handler == null) {
                handler = defaultHandler;
                args = line;
            }
            return handler.handle(args);
        } else {
            return true;
        }
    }
}
