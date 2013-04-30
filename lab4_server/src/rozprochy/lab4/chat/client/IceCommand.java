package rozprochy.lab4.chat.client;

import java.util.Scanner;

import rozprochy.lab4.cli.Command;

abstract class IceCommand implements Command {

    public abstract boolean doExecute(String cmd, Scanner input);
    
    @Override
    public boolean execute(String cmd, Scanner input) {
        try {
            return doExecute(cmd, input);
        } catch (Ice.ConnectFailedException e) {
            System.err.println("Connection failed: " + e.getMessage());
        } catch (Ice.LocalException e) {
            System.err.println("Ice problem");
            e.printStackTrace(System.err);
        }
        return true;
    }
    
}