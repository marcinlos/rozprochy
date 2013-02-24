package rozprochy.rok2011.lab3.zad1.server;

import java.io.IOException;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.UserException;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import rozprochy.rok2011.lab3.zad1.Device;
import rozprochy.rok2011.lab3.zad1.DeviceHelper;
import rozprochy.rok2011.lab3.zad1.DeviceOperations;
import rozprochy.rok2011.lab3.zad1.DevicePOATie;
import rozprochy.rok2011.lab3.zad1.Laboratory;
import rozprochy.rok2011.lab3.zad1.NoSuchDevice;
import rozprochy.rok2011.lab3.zad1.comon.CORBAException;
import rozprochy.rok2011.lab3.zad1.comon.CORBAUtil;
import rozprochy.rok2011.lab3.zad1.comon.StringUtil;
import rozprochy.rok2011.lab3.zad1.destroyer.TankProvider;

public class Server {
    
    private static final String LAB_POA_NAME = "LabPOA";
    private static final String DEV_POA_NAME = "DevPOA";
    
    private ORB orb;
    private NamingContextExt nameService;
    
    private POA rootPOA;
    private POA labPOA;
    private POA devicePOA;
    
    private LaboratoryImpl laboratory;
    private final String labServiceName = Laboratory.SERVICE_NAME;
    
    /**
     * Creates a new server exporting laboratory and device objects.
     * All the initialization takes place in this constructor, when 
     * it returns {@link #run()} can be called.
     * 
     * @param orb ORB object to use
     * @throws CORBAException
     */
    public Server(ORB orb) throws CORBAException {
        this.orb = orb;
        System.out.println("Obtaining name service...");
        this.nameService = getNameService();
        System.out.println("Obtaining root POA...");
        this.rootPOA = getRootPOA();
        
        this.laboratory = new LaboratoryImpl(this);
        
        activate();
        System.out.println("Server initialized.");
    }

    public void run() throws IOException {
        
        @SuppressWarnings("unchecked")
        DeviceProviders providers = new FixedDeviceProvider(TankProvider.class);
        
        CLI cli = new CLI(laboratory, providers);
        cli.run();
    }

    public void cleanup() {
        System.out.println("Server cleanup...");
        if (laboratory != null) {
            laboratory.cleanup();
        }
    }

    /**
     * Activates object corresponding to passed servant. Id is created from
     * value returned by {@link DeviceOperations#name()} invoked on
     * {@code device}.
     * 
     * @param device device implementation to use as a servant
     * @return object reference
     */
    public Device activateDevice(DeviceOperations device) {
        DevicePOATie tie = new DevicePOATie(device, devicePOA);
        byte[] id = StringUtil.encode(device.name());
        try {
            devicePOA.activate_object_with_id(id, tie);
        } catch (ServantAlreadyActive e) {
            System.err.println("Warning: servant for " + device.name() + 
                    " has already been activated");
        } catch (ObjectAlreadyActive e) {
            System.err.println("Warning: object " + device.name() + 
                    " is already active");
        } catch (WrongPolicy e) {
            System.err.println("Device POA is not configured correctly, " + 
                    "RETAIN policy should be specified");
            throw new RuntimeException(e);
        }
        return tie._this();
    }
    
    /**
     * Activates device object and creates its servant from device name.
     * 
     * @param name name of a device to activate (as used by laboratory)
     * @return object reference
     * @throws NoSuchDevice
     */
    public Device activateDevice(String name) throws NoSuchDevice {
        DeviceOperations impl = laboratory.getDevice(name);
        return activateDevice(impl);
    }
    
    /**
     * Returns a device reference, creates the servant if it doesn't already
     * exist. 
     * 
     * @param name name of a device whose reference is to be returned
     * @return reference to an
     * @throws CORBAException
     */
    public Device getDeviceReference(String name) throws NoSuchDevice {
        byte[] id = StringUtil.encode(name);
        return getDeviceReference(id);
    }
    
    
    private Device getDeviceReference(byte[] id) throws NoSuchDevice {
        try {
            Object obj = devicePOA.id_to_reference(id);
            return DeviceHelper.narrow(obj);
        } catch (ObjectNotActive e) {
            String name = StringUtil.decode(id);
            System.err.println("Object " + name + " was inactive");
            return activateDevice(name);
        } catch (WrongPolicy e) {
            System.err.println("Device POA is not configure correctly, " +
                    "RETAIN policy should be specified");
            throw new RuntimeException(e);
        }
    }
    
    
    class LazyDeviceLoader extends LocalObject implements ServantActivator {

        @Override
        public Servant incarnate(byte[] oid, POA adapter) throws ForwardRequest {
            try {
                Device dev = getDeviceReference(oid);
                return devicePOA.reference_to_servant(dev);
            } catch (NoSuchDevice e) {
                String name = StringUtil.decode(oid);
                System.err.println("WARN: Attempt to incarnate nonexistant " + 
                        "device " + name);
            } catch (ObjectNotActive e) {
                throw new RuntimeException(e);
            } catch (UserException e) {
                // The rest of non-system exceptions cannot happen
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        public void etherealize(byte[] oid, POA adapter, Servant serv,
                boolean cleanup_in_progress, boolean remaining_activations) {
            System.err.println("UNIMPLEMENTED: etherealize");
        }
        
    }
    

    /*
     * Obtains reference to name service.
     */
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

    /*
     * Obtains reference to the root POA. Does not activate it.
     */
    private POA getRootPOA() throws CORBAException {
        try {
            Object obj = orb.resolve_initial_references("RootPOA");
            POA poa = POAHelper.narrow(obj);
            return poa;
        } catch (InvalidName e) {
            System.err.println("Cannot obtain a reference to name root POA");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Creates & activates object adapters.
     */
    private void activate() throws CORBAException {
        try {
            System.out.println("Creating laboratory POA...");
            labPOA = makeLabPOA();
            System.out.println("Creating device POA...");
            devicePOA = makeDevPOA();
            System.out.println("Activating POA manager...");
            rootPOA.the_POAManager().activate();
            System.out.println("Activating laboratory...");
            activateLabServant();
            System.out.println("Registering laboratory in Name Service...");
            bindLabName();
        } catch (AdapterInactive e) {
            System.err.println("Attempt to activate the POA manager while" + 
                    "shutting down. Programmer error, maybe?");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Creates POA for the laboratory servant
     */
    private POA makeLabPOA() throws CORBAException {
        try {
            // TODO: Use appropriate policies
            Policy[] policies = new Policy[0];
            POAManager manager = rootPOA.the_POAManager();
            return rootPOA.create_POA(LAB_POA_NAME, manager, policies);
        } catch (AdapterAlreadyExists e) {
            System.err.println("Adapter " + LAB_POA_NAME + " already exists");
            throw new CORBAException(e);
        } catch (InvalidPolicy e) {
            System.err.println("Invalid policy specified for lab POA");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Creates POA for the device servants.
     */
    private POA makeDevPOA() throws CORBAException {
        try {
            Policy[] policies = new Policy[1];
            
            policies[0] = rootPOA.create_request_processing_policy(
                    RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            POAManager manager = rootPOA.the_POAManager();
            POA poa = rootPOA.create_POA(DEV_POA_NAME, manager, policies);
            for (Policy policy : policies) {
                policy.destroy();
            }
            poa.set_servant_manager(new LazyDeviceLoader());
            return poa;
        } catch (AdapterAlreadyExists e) {
            System.err.println("Adapter " + DEV_POA_NAME + " already exists");
            throw new CORBAException(e);
        } catch (InvalidPolicy e) {
            System.err.println("Invalid policy specified for device POA");
            throw new CORBAException(e);
        } catch (WrongPolicy e) {
            System.err.println("Invalid policy specified for device POA - " + 
                    "cannot use servant manager");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Registers laboratory servant in the POA.
     */
    private void activateLabServant() throws CORBAException {
        try {
            labPOA.activate_object(laboratory);
        } catch (ServantAlreadyActive e) {
            System.err.println("Laboratory servant activated more than once");
            throw new CORBAException(e);
        } catch (WrongPolicy e) {
            System.err.println("Laboratory POA policy does not allow " + 
                    "simple activation");
            throw new CORBAException(e);
        }
    }

    /*
     * Binds the laboratory object in the name service.
     */
    private void bindLabName() throws CORBAException {
        try {
            Object labRef = labPOA.servant_to_reference(laboratory);
            NameComponent[] name = nameService.to_name(labServiceName);
            nameService.bind(name, labRef);
        } catch (ServantNotActive e) {
            System.err.println("Cannot obtain laboratory reference, " +
                    "it has not been activated");
            throw new CORBAException(e);
        } catch (WrongPolicy e) {
            System.err.println("Cannot register laboratory at name " + 
                    "service, wrong policy");
            throw new CORBAException(e);
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            System.err.println("Cannot register laboratory, name `" + 
                    labServiceName + "' is invalid");
            throw new CORBAException(e);
        } catch (NotFound e) {
            String reason = CORBAUtil.formatNotFoundReason(e);
            System.err.println(reason);
            System.err.println("(full name: `" + labServiceName + "'");
            throw new CORBAException(e);
        } catch (CannotProceed e) {
            System.err.println("Some problem encountered while going down " + 
                    "the naming contexts tree by using `" + labServiceName + 
                    "'");
            String rest = CORBAUtil.formatName(e.rest_of_name);
            System.err.println("Remaining part of path: " + rest);
            throw new CORBAException(e);
        } catch (AlreadyBound e) {
            System.err.println("Laboratory (" + labServiceName + ") " + 
                    "already bound");
            throw new CORBAException(e);
        }
    }
    
}
