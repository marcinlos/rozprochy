package rozprochy.rok2011.lab3.zad1.client;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.InterfaceDef;

import rozprochy.rok2011.lab3.zad1.Laboratory;
import rozprochy.rok2011.lab3.zad1.LaboratoryPackage.DeviceAlreadyAcquired;
import rozprochy.rok2011.lab3.zad1.LaboratoryPackage.NoSuchDevice;
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
        registerHandler("acquire", cmdAcquire);
        registerHandler("iface", cmdShowInterface);
    }
    
    
    private Command cmdAcquire = new Command() {
        
        @Override
        public boolean execute(String cmd, Scanner input) {
            try {
                String name = input.next();
                client.acquire(name);
            } catch (NoSuchElementException e) {
                System.err.println("Incomplete command; acquire <name>");
            } catch (DeviceAlreadyAcquired e) {
                System.err.println("Device is already acquired");
            } catch (NoSuchDevice e) {
                System.err.println("Device doesn't exist");
            }
            return true;
        }
    };
    
    private Command cmdShowInterface = new Command() {

        @Override
        public boolean execute(String cmd, Scanner input) {
            try {
                String name = input.next();
                InterfaceDef iface = client.getDeviceInterface(name);
                
                System.out.println("Type: " + iface.name());
            } catch (NoSuchElementException e) {
                System.err.println("Incomplete command; iface <name>");
            } catch (NoSuchDevice e) {
                System.err.println("Device not acquired");
            } catch (INTF_REPOS e) { 
                if (e.minor == 1) {
                    System.err.println("Interface repository not available");
                } else if (e.minor == 2) {
                    System.err.println("Interface not found in IR");
                }
            }
            return true;
        }
    };
    
}

