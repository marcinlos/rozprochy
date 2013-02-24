package rozprochy.rok2011.lab3.zad1.server;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import rozprochy.rok2011.lab3.zad1.provider.DeviceFactory;

public class DefaultDeviceProviders implements DeviceProviders {

    private Map<String, DeviceFactory> providers = 
            new HashMap<String, DeviceFactory>();
    
    public DefaultDeviceProviders() {
        System.out.println("Loading devices...");
        ServiceLoader<DeviceFactory> loader = 
                ServiceLoader.load(DeviceFactory.class);
        for (DeviceFactory provider : loader) {
            String name = provider.getTypeName();
            providers.put(name, provider);
            System.out.println("* " + name);
        }
        System.out.println("Devices loaded.");
    }

    @Override
    public Map<String, DeviceFactory> getProviders() {
        return providers;
    }

}
