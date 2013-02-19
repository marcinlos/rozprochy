package rozprochy.corba.test.server;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import rozprochy.corba.test.Greeter;
import rozprochy.corba.test.GreeterHelper;

public class Server {
    
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            Object o = orb.resolve_initial_references("RootPOA");
            POA rootpoa = POAHelper.narrow(o);
            rootpoa.the_POAManager().activate();
            
            // Creating servant
            GreeterImpl greeter = new GreeterImpl();
            Object ref = rootpoa.servant_to_reference(greeter);
            Greeter gref = GreeterHelper.narrow(ref);
            
            System.out.println("Resolving nameservice...");
            Object oRef = orb.resolve_initial_references("NameService");
            NamingContextExt nc = NamingContextExtHelper.narrow(oRef);
            System.out.println("Found nameservice");
            
            NameComponent[] path = nc.to_name("StringService");
            nc.rebind(path, gref);
            System.out.println("Object registered");
            
            orb.run();
            orb.destroy();
            
        } catch (InvalidName e) {
            e.printStackTrace();
        } catch (AdapterInactive e) {
            e.printStackTrace();
        } catch (ServantNotActive e) {
            e.printStackTrace();
        } catch (WrongPolicy e) {
            e.printStackTrace();
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            e.printStackTrace();
        } catch (NotFound e) {
            e.printStackTrace();
        } catch (CannotProceed e) {
            e.printStackTrace();
        }
    }

}
