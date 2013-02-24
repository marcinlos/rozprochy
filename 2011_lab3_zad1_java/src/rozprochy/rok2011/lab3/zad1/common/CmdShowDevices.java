package rozprochy.rok2011.lab3.zad1.common;

import java.util.Scanner;

import rozprochy.rok2011.lab3.zad1.DeviceDesc;
import rozprochy.rok2011.lab3.zad1.LaboratoryOperations;

public class CmdShowDevices implements Command {
    
    private LaboratoryOperations laboratory;
    
    public CmdShowDevices(LaboratoryOperations laboratory) {
        this.laboratory = laboratory;
    }

    @Override
    public boolean execute(String cmd, Scanner input) {
        DeviceDesc[] devices = laboratory.allDevices();
        if (devices.length != 0) {
            int i = 0;
            for (DeviceDesc device : devices) {
                System.out.println(formatInfo(device, ++ i));
            }
        } else {
            System.out.println("(no devices)");
        }
        return true;
    }

    private String formatInfo(DeviceDesc device, int i) {
        String controlled = device.free ? "" : "* ";
        return String.format("%d. %s (%s)%s [%d]", i, device.name,
                device.type, controlled, device.watchers);
    }
}