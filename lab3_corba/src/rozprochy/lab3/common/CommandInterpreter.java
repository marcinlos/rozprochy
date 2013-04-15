package rozprochy.lab3.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Command interpreter based on map of actions.
 */
public class CommandInterpreter extends AbstractCLI {

    private Map<String, Command> handlers = new HashMap<String, Command>();

    /* Default handler, used for missing commands */
    private final Command defaultHandler = new Command() {
        @Override
        public boolean execute(String name, Scanner input) {
            return missingCommand(name, input);
        }
    };

    /**
     * @sa {@link AbstractCLI#AbstractCLI()}
     */
    public CommandInterpreter() throws IOException {
    }

    /**
     * @sa {@link AbstractCLI#AbstractCLI(Reader)}
     */
    public CommandInterpreter(Reader reader) {
        super(reader);
    }

    /**
     * @sa {@link AbstractCLI#AbstractCLI(InputStream)}
     */
    public CommandInterpreter(InputStream stream) {
        super(stream);
    }
    
    /**
     * Registers a new command.
     * 
     * @param name name of the command
     * @param handler command implementation
     */
    public void registerHandler(String name, Command handler) {
        handlers.put(name, handler);
    }

    /**
     * Placeholder for missing commands.
     * 
     * @param name name of the missing command
     * @param rest scanner that have consumed the first word of the input line
     * @return {@code true} if the input loop should continue, {@code false}
     *         if it should be terminated immediately
     */
    protected boolean missingCommand(String name, Scanner rest) {
        System.err.println("Unknown command `" + name + "'");
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean interpret(String line) {
        Scanner scanner = new Scanner(line);
        String name = scanner.next();
        Command handler = getHandler(name);
        return handler.execute(name, scanner);
    }
    
    private Command getHandler(String name) {
        Command command = handlers.get(name);
        if (command == null) {
            return defaultHandler;
        } else {
            return command;
        }
    }

}
