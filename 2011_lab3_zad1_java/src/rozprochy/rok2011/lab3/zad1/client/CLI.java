package rozprochy.rok2011.lab3.zad1.client;

import java.io.IOException;

import rozprochy.rok2011.lab3.zad1.Laboratory;
import rozprochy.rok2011.lab3.zad1.common.CommandInterpreter;

/**
 * Client command line interface
 */
public class CLI extends CommandInterpreter {

    private Laboratory laboratory;
    private Client client;

    public CLI(Client client, Laboratory laboratory) throws IOException {
        this.laboratory = laboratory;
        this.client = client;
    }

}
