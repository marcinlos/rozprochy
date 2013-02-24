package rozprochy.rok2011.lab3.zad1.server;

import java.util.HashMap;
import java.util.Map;

import rozprochy.rok2011.lab3.zad1.AcquireMode;
import rozprochy.rok2011.lab3.zad1.Device;
import rozprochy.rok2011.lab3.zad1.DeviceAlreadyAcquired;
import rozprochy.rok2011.lab3.zad1.DeviceDesc;
import rozprochy.rok2011.lab3.zad1.DeviceNotAcquired;
import rozprochy.rok2011.lab3.zad1.DeviceOperations;
import rozprochy.rok2011.lab3.zad1.LaboratoryPOA;
import rozprochy.rok2011.lab3.zad1.NoSuchDevice;
import rozprochy.rok2011.lab3.zad1.provider.DeviceFactory;

/**
 * Concrete implementation of laboratory interface.
 */
public class LaboratoryImpl extends LaboratoryPOA {

    /* List of devices without servant - servants are created lazily */
    private Map<String, DeviceInstance> devices = 
            new HashMap<String, DeviceInstance>();

    /* Server */
    private final Server server;

    public LaboratoryImpl(Server server) {
        this.server = server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DeviceDesc[] allDevices() {
        int n = devices.size();
        DeviceDesc[] descs = new DeviceDesc[n];

        int i = 0;
        for (DeviceInstance device : devices.values()) {
            descs[i++] = makeDescription(device);
        }

        System.out.println("addDevices()");
        return descs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Device acquireDevice(String name, AcquireMode mode)
            throws DeviceAlreadyAcquired, NoSuchDevice {
        System.out.println("acquireDevice()");
        return server.getDeviceReference(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void releaseDevice(String name)
            throws DeviceNotAcquired, NoSuchDevice {
        System.out.println("releaseDevice()");
    }

    /**
     * 
     * @param name
     * @return
     * @throws NoSuchDevice
     */
    public DeviceOperations getDevice(String name) throws NoSuchDevice {
        DeviceInstance device;
        synchronized (this) {
            device = devices.get(name);
        }
        if (device != null) {
            return device.getServant();
        } else {
            throw new NoSuchDevice(name);
        }
    }
    
    /**
     * Adds a new device (name + servant factory) to the laboratory.
     * 
     * @param name name of the device
     * @param factory factory used to create servants 
     * @throws DeviceAlreadyExists when a device with the same name is already
     *         present in the lab.
     */
    public void registerDevice(String name, DeviceFactory factory) 
            throws DeviceAlreadyExists {
        if (! devices.containsKey(name)) {
            DeviceInstance instance = new DeviceInstance(name, factory);
            devices.put(name, instance);
        } else {
            throw new DeviceAlreadyExists(name);
        }
    }

    public void cleanup() {

    }

    private static DeviceDesc makeDescription(DeviceInstance device) {
        return new DeviceDesc(device.getName(), device.getType());
    }

}
