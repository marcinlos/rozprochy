package rozprochy.lab5.cli;

import java.util.Scanner;

/**
 * Command handler interface, used by the {@link CommandInterpreter}.
 */
public interface Command {

    /**
     * The scanner has consumed the first word (command name), consecutive
     * calls to {@code next*} shall return parameters.
     * 
     * @param cmd name by which the command has been invoked
     * @param input {@code Scanner} created from a single line of input
     * @return {@code true} if the input loop should continue, 
     *         {@code false} if it should be terminated immediately
     */
    boolean execute(String cmd, Scanner input);
}