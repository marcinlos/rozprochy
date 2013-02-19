package rozprochy.corba.test.client;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import rozprochy.corba.test.Greeter;
import rozprochy.corba.test.GreeterHelper;

public class Client {
    
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            
            System.out.println("Resolving nameservice...");
            Object oRef = orb.resolve_initial_references("NameService");
            NamingContextExt nc = NamingContextExtHelper.narrow(oRef);
            
            System.out.println("Found nameservice");
            
            Object serviceRef = nc.resolve_str("StringService");
            Greeter greeter = GreeterHelper.narrow(serviceRef);

            System.out.println("Greeter: " + greeter.greet("zbyszek"));
        } catch (InvalidName e) {
            e.printStackTrace(System.err);
        } catch (NotFound e) {
            e.printStackTrace(System.err);
        } catch (CannotProceed e) {
            e.printStackTrace(System.err);
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            e.printStackTrace(System.err);
        }
    }
    
}
