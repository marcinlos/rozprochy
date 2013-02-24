package rozprochy.rok2011.lab3.zad1.server;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import rozprochy.rok2011.lab3.zad1.common.CmdShowDevices;
import rozprochy.rok2011.lab3.zad1.common.Command;
import rozprochy.rok2011.lab3.zad1.common.CommandInterpreter;
import rozprochy.rok2011.lab3.zad1.provider.DeviceFactory;

/**
 * Command line interface interpreter for the server.
 */
public class CLI extends CommandInterpreter {

    private final LaboratoryImpl laboratory;
    private final DeviceProviders providers;

    /**
     * Creates a command line interface acting upon {@code laboratory}
     */
    public CLI(LaboratoryImpl laboratory, DeviceProviders providers) 
            throws IOException {
        this.laboratory = laboratory;
        this.providers = providers;
        
        this.cmdShowDevices = new CmdShowDevices(laboratory);
        
        registerHandler("add", cmdAdd);
        registerHandler("show", cmdShowDevices);
        registerHandler("types", cmdShowTypes);
    }

    
    private Command cmdAdd = new Command() {
        @Override
        public boolean execute(String cmd, Scanner input) {
            try {
                String type = input.next();
                String name = input.next();
                DeviceFactory provider = providers.getProviders().get(type);
                if (provider != null) {
                    laboratory.registerDevice(name, provider);
                } else {
                    System.err.println("Unknown device `" + type + "'");
                }
            } catch (DeviceAlreadyExists e) {
                System.err.println("Device " + e.getName() + " already exists");
            } catch (NoSuchElementException e) {
                System.err.println("incomplete command; add <type> <name>");
            }
            return true;
        }
    };
    
    private Command cmdShowDevices;
    
    private Command cmdShowTypes = new Command() {

        @Override
        public boolean execute(String cmd, Scanner input) {
            Map<String, DeviceFactory> ps = providers.getProviders();
            if (! ps.isEmpty()) {
                for (DeviceFactory provider : ps.values()) {
                    System.out.println(provider.getTypeName());
                }
            } else {
                System.out.println("(no device providers)");
            }
            return true;
        }
        
    };
    
}
