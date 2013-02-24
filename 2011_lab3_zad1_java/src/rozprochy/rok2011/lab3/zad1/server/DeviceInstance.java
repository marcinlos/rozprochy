package rozprochy.rok2011.lab3.zad1.server;

import rozprochy.rok2011.lab3.zad1.DeviceOperations;
import rozprochy.rok2011.lab3.zad1.provider.DeviceFactory;

/**
 * Simple auxilary class managing one device instance.
 */
public class DeviceInstance {

    private String name;
    private DeviceFactory creator;
    private DeviceOperations servant;

    public DeviceInstance(String name, DeviceFactory creator) {
        this.name = name;
        this.creator = creator;
    }

    /**
     * @return name of the device instance
     */
    public String getName() {
        return name;
    }

    /**
     * Returns servant object, creates it when needen. Thread-safe.
     */
    public synchronized DeviceOperations getServant() {
        if (servant == null) {
            servant = creator.newInstance(name);
        }
        return servant;
    }

    /**
     * @return {@code true} if an object has a servant, {@code false} otherwise.
     */
    public synchronized boolean hasServant() {
        return servant == null;
    }

    /**
     * @return type of the device (e.g. class name)
     */
    public String getType() {
        // TODO: Provide real implementation
        return "device";
    }

}
