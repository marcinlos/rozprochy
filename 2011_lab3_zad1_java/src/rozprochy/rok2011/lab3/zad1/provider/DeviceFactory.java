package rozprochy.rok2011.lab3.zad1.provider;

import rozprochy.rok2011.lab3.zad1.DeviceOperations;

/**
 * Abstract factory of device implementations.
 */
public interface DeviceFactory {
    
    DeviceOperations newInstance(String name);
    
    String getTypeName();

}
