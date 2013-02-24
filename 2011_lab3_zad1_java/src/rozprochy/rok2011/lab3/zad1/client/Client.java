package rozprochy.rok2011.lab3.zad1.client;

import java.io.IOException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import rozprochy.rok2011.lab3.zad1.Laboratory;
import rozprochy.rok2011.lab3.zad1.LaboratoryHelper;
import rozprochy.rok2011.lab3.zad1.common.CORBAException;
import rozprochy.rok2011.lab3.zad1.common.CORBAUtil;

public class Client {

    private ORB orb;
    private NamingContextExt nameService;

    private Laboratory laboratory;
    private final String labServiceName = Laboratory.SERVICE_NAME;

    public Client(ORB orb) throws CORBAException {
        this.orb = orb;
        this.nameService = getNameService();
        this.laboratory = getLaboratory();
    }

    public void run() throws IOException {
        CLI cli = new CLI();
        cli.run();
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