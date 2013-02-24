package rozprochy.rok2011.lab3.zad1.server;

import java.util.Map;

import rozprochy.rok2011.lab3.zad1.provider.DeviceFactory;

/**
 * Interface of a source of device providers (e.g. by ServiceLoader, fixed list
 * etc). 
 */
public interface DeviceProviders {

    /**
     * @return map of discovered device providers
     */
    Map<String, DeviceFactory> getProviders();
    
}
