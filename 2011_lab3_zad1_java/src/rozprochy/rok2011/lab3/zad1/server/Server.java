package rozprochy.rok2011.lab3.zad1.server;

import java.io.IOException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import rozprochy.rok2011.lab3.zad1.Laboratory;
import rozprochy.rok2011.lab3.zad1.comon.CORBAException;
import rozprochy.rok2011.lab3.zad1.comon.Util;

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

    public Server(ORB orb) throws CORBAException {
        this.orb = orb;
        System.out.println("Obtaining name service...");
        this.nameService = getNameService();
        System.out.println("Obtaining root POA...");
        this.rootPOA = getRootPOA();
        this.laboratory = new LaboratoryImpl();
        
        activate();
        
        System.out.println("Server initialized.");
    }

    public void run() throws IOException {
        CLI cli = new CLI(laboratory);
        cli.run();
    }

    public void cleanup() {
        System.out.println("Server cleanup...");
        if (laboratory != null) {
            laboratory.cleanup();
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
            // TODO: Use appropriate policies
            Policy[] policies = new Policy[0];
            POAManager manager = rootPOA.the_POAManager();
            return rootPOA.create_POA(DEV_POA_NAME, manager, policies);
        } catch (AdapterAlreadyExists e) {
            System.err.println("Adapter " + DEV_POA_NAME + " already exists");
            throw new CORBAException(e);
        } catch (InvalidPolicy e) {
            System.err.println("Invalid policy specified for lab POA");
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
            String reason = Util.formatNotFoundReason(e);
            System.err.println(reason);
            System.err.println("(full name: `" + labServiceName + "'");
            throw new CORBAException(e);
        } catch (CannotProceed e) {
            System.err.println("Some problem encountered while going down " + 
                    "the naming contexts tree by using `" + labServiceName + 
                    "'");
            String rest = Util.formatName(e.rest_of_name);
            System.err.println("Remaining part of path: " + rest);
            throw new CORBAException(e);
        } catch (AlreadyBound e) {
            System.err.println("Laboratory (" + labServiceName + ") " + 
                    "already bound");
            throw new CORBAException(e);
        }
    }
    
}
