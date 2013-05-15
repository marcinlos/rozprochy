package rozprochy.lab5.cli;

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
    private String prompt = "";

    /* Default handler, used for missing commands */
    private Command defaultHandler = new Command() {
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
     * Set prompt
     * 
     * @param prompt new prompt to use
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
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
     * Registers custom default handler.
     * 
     * @param handler handler to be used as a default
     */
    public void setDefaultHandler(Command handler) {
        defaultHandler = handler;
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
        if (! line.isEmpty()) {
            Scanner scanner = new Scanner(line);
            String name = scanner.next();
            Command handler = getHandler(name);
            return handler.execute(name, scanner);
        } else {
            return true;
        }
    }
    
    private Command getHandler(String name) {
        Command command = handlers.get(name);
        if (command == null) {
            return defaultHandler;
        } else {
            return command;
        }
    }

    @Override
    protected void afterLine() {
        System.out.print("\r" + prompt);
        System.out.flush();
    }

}
