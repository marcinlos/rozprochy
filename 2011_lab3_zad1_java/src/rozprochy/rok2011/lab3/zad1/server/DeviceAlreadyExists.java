package rozprochy.rok2011.lab3.zad1.server;

/**
 * Exception thrown as a result of an attempt to register a device with name
 * already bound to another registered device.
 */
public class DeviceAlreadyExists extends LabException {

    private String name;

    public DeviceAlreadyExists(String name) {
        this.name = name;
    }

    /**
     * @return name which was used in a register attempt
     */
    public String getName() {
        return name;
    }

}
