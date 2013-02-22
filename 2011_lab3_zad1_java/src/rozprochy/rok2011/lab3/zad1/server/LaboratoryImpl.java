package rozprochy.rok2011.lab3.zad1.server;

import rozprochy.rok2011.lab3.zad1.AcquireMode;
import rozprochy.rok2011.lab3.zad1.Device;
import rozprochy.rok2011.lab3.zad1.DeviceAlreadyAcquired;
import rozprochy.rok2011.lab3.zad1.DeviceDesc;
import rozprochy.rok2011.lab3.zad1.DeviceNotAcquired;
import rozprochy.rok2011.lab3.zad1.LaboratoryPOA;
import rozprochy.rok2011.lab3.zad1.NoSuchDevice;

public class LaboratoryImpl extends LaboratoryPOA {

    @Override
    public DeviceDesc[] allDevices() {
        // TODO Auto-generated method stub
        System.out.println("addDevices()");
        return null;
    }

    @Override
    public Device acquireDevice(String name, AcquireMode mode)
            throws DeviceAlreadyAcquired, NoSuchDevice {
        // TODO Auto-generated method stub
        System.out.println("acquireDevice()");
        return null;
    }

    @Override
    public void releaseDevice(String name) throws DeviceNotAcquired,
            NoSuchDevice {
        // TODO Auto-generated method stub
        System.out.println("releaseDevice()");
        
    }
    
    public void cleanup() {
        
    }

}
