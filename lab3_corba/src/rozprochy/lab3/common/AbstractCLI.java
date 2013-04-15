package rozprochy.lab3.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Auxilary base class for creating command line interfaces.
 */
public abstract class AbstractCLI {

    /**
     * Input source
     */
    protected final BufferedReader input;

    /**
     * Creates a command line interface using standard input.
     */
    public AbstractCLI() throws IOException {
        input = new BufferedReader(new InputStreamReader(System.in));
    }
    
    /**
     * Creates a command line interface using given {@code Reader} instance
     * as an input source.
     */
    public AbstractCLI(Reader reader) {
        // Check whether the reader already is buffered
        if (reader instanceof BufferedReader) {
            input = (BufferedReader) reader;
        } else {
            input = new BufferedReader(reader);
        }
    }
    
    /**
     * Creates a command line interface using given {@code Stream} instance
     * as an input source.
     */
    public AbstractCLI(InputStream stream) {
        this(new InputStreamReader(stream));
    }

    /**
     * Input-consuming and interpreting loop.
     * 
     * @throws IOException if reading the standard input fails
     */
    public void run() throws IOException {

        String line;
        while ((line = input.readLine()) != null) {
            if (! line.isEmpty()) {
                if (! interpret(line)) {
                    break;
                }
            }
        }
    }

    /**
     * Actual interpreting function, gets its input from {@link #run()} and
     * processes it. Does not allow checked exceptions - they sould be dealt
     * with inside the interpret function.
     * 
     * @param line whole line from the terminal
     * @return {@code true} if the server should continue running, 
     *         {@code false} otherwise.
     */
    protected abstract boolean interpret(String line);
}
