package rozprochy.rok2011.lab3.zad1.client;

import java.io.IOException;

import rozprochy.rok2011.lab3.zad1.Laboratory;
import rozprochy.rok2011.lab3.zad1.common.CmdShowDevices;
import rozprochy.rok2011.lab3.zad1.common.Command;
import rozprochy.rok2011.lab3.zad1.common.CommandInterpreter;

/**
 * Client command line interface
 */
public class CLI extends CommandInterpreter {

    private Laboratory laboratory;
    private Client client;
    private Command cmdShowDevices;
    
    public CLI(Client client, Laboratory laboratory) throws IOException {
        this.laboratory = laboratory;
        this.client = client;
        this.cmdShowDevices = new CmdShowDevices(laboratory);
        
        registerHandler("show", cmdShowDevices);
    }
    
}

