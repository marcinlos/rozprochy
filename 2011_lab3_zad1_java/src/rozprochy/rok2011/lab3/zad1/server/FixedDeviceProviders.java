package rozprochy.rok2011.lab3.zad1.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import rozprochy.rok2011.lab3.zad1.provider.DeviceFactory;

public class FixedDeviceProviders implements DeviceProviders {

    private Map<String, DeviceFactory> providers = new HashMap<String, DeviceFactory>();

    public FixedDeviceProviders(Class<? extends DeviceFactory>... classes) {
        for (Class<? extends DeviceFactory> clazz : classes) {
            try {
                Constructor<? extends DeviceFactory> ctr = getCtor(clazz);
                DeviceFactory instance = ctr.newInstance(new Object[0]);
                String name = instance.getTypeName();
                providers.put(name, instance);

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static <T> Constructor<T> getCtor(Class<T> clazz)
            throws SecurityException, NoSuchMethodException {
        return clazz.getConstructor(new Class[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, DeviceFactory> getProviders() {
        return providers;
    }

}
