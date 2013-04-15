package rozprochy.lab3.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.omg.CORBA.ARG_IN;
import org.omg.CORBA.Any;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Request;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import rozprochy.lab3.MiddlewareTestbed.AFactory;
import rozprochy.lab3.MiddlewareTestbed.AFactoryHelper;
import rozprochy.lab3.MiddlewareTestbed.Item;
import rozprochy.lab3.MiddlewareTestbed.ItemAlreadyExists;
import rozprochy.lab3.MiddlewareTestbed.ItemBusy;
import rozprochy.lab3.MiddlewareTestbed.ItemNotExists;
import rozprochy.lab3.common.CORBAException;
import rozprochy.lab3.common.CORBAUtil;
import rozprochy.lab3.common.Command;
import rozprochy.lab3.common.CommandInterpreter;

public class Client {

    private ORB orb;
    private NamingContextExt nameService;

    private AFactory factory;
    private final String serviceName;
    
    /** Map of items acquired on the server */
    private Map<String, Item> acquiredItems = new HashMap<String, Item>();
    
    private CommandInterpreter cli;
    

    public Client(ORB orb) throws CORBAException {
        this.orb = orb;
        this.serviceName = System.getProperty("factory.name", "factory");
        System.out.println("Obtaining name service...");
        this.nameService = getNameService();
        System.out.println("Obtaining factory reference...");
        this.factory = getFactory();
        System.out.println("Done.");
    }

    
    public void run() throws IOException {
        cli = new CommandInterpreter();
        cli.registerHandler("create", new Command() {
            @Override public boolean execute(String cmd, Scanner input) {
                try {
                    String type = input.next(), name = input.next();
                    createItem(name, type);
                } catch (NoSuchElementException e) {
                    System.out.println("Usage: create <type> <name>");
                } catch (ItemAlreadyExists e) {
                    System.out.println("Error: item already exists");
                }
                return true;
            }
        });
        cli.registerHandler("take", new Command() {
            @Override public boolean execute(String cmd, Scanner input) {
                try {
                    String name = input.next();
                    takeItem(name);
                } catch (NoSuchElementException e) {
                    System.out.println("Usage: take <name>");
                } catch (ItemNotExists e) {
                    System.out.println("Error: item does not exist");
                } catch (ItemBusy e) {
                    System.out.println("Error: item is busy");
                }
                return true;
            }
        });
        cli.registerHandler("release", new Command() {
            @Override public boolean execute(String cmd, Scanner input) {
                try {
                    String name = input.next();
                    releaseItem(name);
                } catch (NoSuchElementException e) {
                    System.out.println("Usage: take <name>");
                } catch (ItemNotExists e) {
                    System.out.println("Error: item does not exist");
                }
                return true;
            }
        });
        cli.registerHandler("age", new Command() {
            
            @Override
            public boolean execute(String cmd, Scanner input) {
                try {
                    String name = input.next();
                    Item item = acquiredItems.get(name);
                    if (item != null) {
                        System.out.println("age: " + item.get_item_age() + "s");
                    } else {
                        System.out.println("Error: item does not exist");
                    }
                } catch (NoSuchElementException e) {
                    System.out.println("Usage: age <name>");
                }
                return true;
            }
        });
        cli.registerHandler("call", new Command() {
            @Override public boolean execute(String cmd, Scanner input) {
                try {
                    String name = input.next();
                    Item item = acquiredItems.get(name);
                    Request thisReq = new DIIParser(input, item).create();
                    thisReq.invoke();

                    StringBuilder sb = new StringBuilder();
                    Any ret = thisReq.return_value();
                    sb.append("ret: ").append(CORBAUtil.anyToString(ret));
                    NVList args = thisReq.arguments();
                    for (int i = 0; i < args.count(); ++ i) {
                        NamedValue val = args.item(i);
                        if ((val.flags() & ARG_IN.value) == 0) {
                            String str = CORBAUtil.anyToString(val.value());
                            System.out.println(val.name() + ": " + str);
                        }
                    }
                    
                } catch (DIIException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Usage: call <name> <operation> " + 
                            "<ret_type> [args...]");
                } catch (Bounds e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        cli.run();
    }
    
    
    public void createItem(String name, String type) throws ItemAlreadyExists {
        Item item = factory.create_item(name, type);
        acquiredItems.put(name, item);
    }
    
    public void takeItem(String name) throws ItemNotExists, ItemBusy {
        Item item = factory.take_item(name);
        acquiredItems.put(name, item);
    }
    
    public void releaseItem(String name) throws ItemNotExists {
        factory.release_item(name);
        acquiredItems.remove(name);
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
     * Obtains factory reference
     */
    private AFactory getFactory() throws CORBAException {
        try {
            Object obj = nameService.resolve_str(serviceName);
            return AFactoryHelper.narrow(obj);
        } catch (NotFound e) {
            System.err.println("ServantFactory (" + serviceName + ") " + 
                    "not found by the name service");
            String reason = CORBAUtil.formatNotFoundReason(e);
            System.err.println(reason);
            throw new CORBAException(e);
        } catch (CannotProceed e) {
            System.err.println("Some problem encountered while resolving " + 
                    "factory name (" + serviceName + ")");
            System.err.println("Only part of the path resolved");
            String rest = CORBAUtil.formatName(e.rest_of_name);
            System.err.println("Remaining part of path: " + rest);
            throw new CORBAException(e);
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            System.err.println("ServantFactory name (" + serviceName + 
                    ") is invalid");
            throw new CORBAException(e);
        }
    }
}