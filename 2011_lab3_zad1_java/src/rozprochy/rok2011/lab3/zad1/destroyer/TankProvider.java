package rozprochy.rok2011.lab3.zad1.destroyer;

import rozprochy.rok2011.lab3.zad1.DeviceOperations;
import rozprochy.rok2011.lab3.zad1.provider.DeviceFactory;

public class TankProvider implements DeviceFactory {

    @Override
    public DeviceOperations newInstance(String name) {
        return new TankImpl(name);
    }

    @Override
    public String getTypeName() {
        return "Tank";
    }

}
