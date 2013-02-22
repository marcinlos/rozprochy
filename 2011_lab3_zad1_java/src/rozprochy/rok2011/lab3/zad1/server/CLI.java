package rozprochy.rok2011.lab3.zad1.server;

import java.io.IOException;

import rozprochy.rok2011.lab3.zad1.comon.AbstractCLI;

/**
 * Command line interface interpreter for the server.
 */
public class CLI extends AbstractCLI {

    private final LaboratoryImpl laboratory;

    /**
     * Creates a command line interface acting upon {@code laboratory}
     */
    public CLI(LaboratoryImpl laboratory) throws IOException {
        this.laboratory = laboratory;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean interpret(String line) {
        // TODO: Act upon the laboratory
        return true;
    }

}
