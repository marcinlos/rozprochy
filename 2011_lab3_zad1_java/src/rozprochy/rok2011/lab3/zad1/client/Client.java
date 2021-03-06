package rozprochy.rok2011.lab3.zad1.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import rozprochy.rok2011.lab3.zad1.Device;
import rozprochy.rok2011.lab3.zad1.Laboratory;
import rozprochy.rok2011.lab3.zad1.LaboratoryHelper;
import rozprochy.rok2011.lab3.zad1.LaboratoryPackage.AcquireMode;
import rozprochy.rok2011.lab3.zad1.LaboratoryPackage.DeviceAlreadyAcquired;
import rozprochy.rok2011.lab3.zad1.LaboratoryPackage.NoSuchDevice;
import rozprochy.rok2011.lab3.zad1.common.CORBAException;
import rozprochy.rok2011.lab3.zad1.common.CORBAUtil;

public class Client {

    private ORB orb;
    private NamingContextExt nameService;

    private Laboratory laboratory;
    private final String labServiceName = Laboratory.SERVICE_NAME;
    
    /** Map of devices acquired on the server */
    private Map<String, Device> controlledDevices = new HashMap<String, Device>();
    

    public Client(ORB orb) throws CORBAException {
        this.orb = orb;
        this.nameService = getNameService();
        this.laboratory = getLaboratory();
    }

    
    public void run() throws IOException {
        CLI cli = new CLI(this, laboratory);
        cli.run();
    }
    
    public void acquire(String name) throws DeviceAlreadyAcquired, NoSuchDevice {
        Device device = laboratory.acquireDevice(name, AcquireMode.control);
        controlledDevices.put(name, device);
        System.out.println("Device acquired");

    }
    
    public InterfaceDef getDeviceInterface(String name) throws NoSuchDevice {
        Device device = controlledDevices.get(name);
        if (device != null) {
            Object ifaceObj = device._get_interface_def();
            InterfaceDef iface = InterfaceDefHelper.narrow(ifaceObj);
            return iface;
        } else {
            throw new NoSuchDevice(name);
        }
    }
    
    private NamingContextExt getNameService() throws CORBAException {
        try {
            Object obj = orb.resolve_initial_references("NameService");
            NamingContextExt nameService = NamingContextExtHelper.narrow(obj);
            return nameService;
        } catch (InvalidName e) {
            System.err.println("Cannot obtain a reference to name service");
            System.err.println("Make sure it's running and its address is "
                    + "correctly passed");
            throw new CORBAException(e);
        }
    }
    
    
    /*private Repository getInterfaceRepository() throws CORBAException {
        try {
            Object obj = orb.resolve_initial_references("InterfaceRepository");
        } finally {
            
        }
    }*/

    /*
     * Obtains laboratory reference
     */
    private Laboratory getLaboratory() throws CORBAException {
        try {
            Object obj = nameService.resolve_str(labServiceName);
            return LaboratoryHelper.narrow(obj);
        } catch (NotFound e) {
            System.err.println("Laboratory (" + labServiceName + ") " + 
                    "not found by the name service");
            String reason = CORBAUtil.formatNotFoundReason(e);
            System.err.println(reason);
            throw new CORBAException(e);
        } catch (CannotProceed e) {
            System.err.println("Some problem encountered while resolving " + 
                    "laboratory name (" + labServiceName + ")");
            System.err.println("Only part of the path resolved");
            String rest = CORBAUtil.formatName(e.rest_of_name);
            System.err.println("Remaining part of path: " + rest);
            throw new CORBAException(e);
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            System.err.println("Labolatory name (" + labServiceName + 
                    ") is invalid");
            throw new CORBAException(e);
        }
    }
}