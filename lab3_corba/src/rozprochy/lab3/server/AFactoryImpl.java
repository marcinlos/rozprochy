package rozprochy.lab3.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import rozprochy.lab3.MiddlewareTestbed.AFactoryPOA;
import rozprochy.lab3.MiddlewareTestbed.Item;
import rozprochy.lab3.MiddlewareTestbed.ItemAOperations;
import rozprochy.lab3.MiddlewareTestbed.ItemAPOATie;
import rozprochy.lab3.MiddlewareTestbed.ItemAlreadyExists;
import rozprochy.lab3.MiddlewareTestbed.ItemBOperations;
import rozprochy.lab3.MiddlewareTestbed.ItemBPOATie;
import rozprochy.lab3.MiddlewareTestbed.ItemBusy;
import rozprochy.lab3.MiddlewareTestbed.ItemCOperations;
import rozprochy.lab3.MiddlewareTestbed.ItemCPOATie;
import rozprochy.lab3.MiddlewareTestbed.ItemHelper;
import rozprochy.lab3.MiddlewareTestbed.ItemNotExists;
import rozprochy.lab3.MiddlewareTestbed.ItemOperations;
import rozprochy.lab3.server.items.ItemAImpl;
import rozprochy.lab3.server.items.ItemBImpl;
import rozprochy.lab3.server.items.ItemCImpl;

public class AFactoryImpl extends AFactoryPOA {
    
    private static class ItemState {
        private boolean taken = false;
        private Item item;
        
        public ItemState(Item item) {
            this.item = item;
        }
        
        public Item getItem() {
            return item;
        }
        
        public boolean take() {
            boolean wasFree = this.taken;
            this.taken = true;
            return !wasFree;
        }
        
        public void release() {
            this.taken = false;
        }
    }
    
    private POA poa;
    private Map<String, ItemState> items = new HashMap<String, ItemState>();
    private final Object lock = new Object();
    
    private ServantFactory defaultFactor = new BrutalFactory(ItemAImpl.class, 
            ItemAOperations.class, ItemAPOATie.class);
    private Map<String, ServantFactory> factories = 
            new HashMap<String, ServantFactory>();
    
    /*
     * Uses one-argument string constructor, if there is one 
     */
    private class BrutalFactory implements ServantFactory {
        
        private Constructor<? extends ItemOperations>  itemCtor;
        private Constructor<? extends Servant> servantCtor;

        public BrutalFactory(
                Class<? extends ItemOperations> itemClazz,
                Class<? extends ItemOperations> itemIface,
                Class<? extends Servant> servantClazz) {
            try {
                itemCtor = itemClazz.getConstructor(String.class);
                servantCtor = servantClazz.getConstructor(itemIface);
            } catch (SecurityException e) {
                throw new AssertionError(itemClazz);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(itemClazz);
            }
        }
        
        @Override
        public Servant create(String name) {
            try {
                ItemOperations obj = itemCtor.newInstance(name);
                return servantCtor.newInstance(obj);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public AFactoryImpl(POA poa) {
        this.poa = poa;
        factories.put("ItemA", new BrutalFactory(ItemAImpl.class, 
                ItemAOperations.class, ItemAPOATie.class));
        factories.put("ItemB", new BrutalFactory(ItemBImpl.class, 
                ItemBOperations.class, ItemBPOATie.class));
        factories.put("ItemC", new BrutalFactory(ItemCImpl.class, 
                ItemCOperations.class, ItemCPOATie.class));
    }

    @Override
    public Item create_item(String name, String type) throws ItemAlreadyExists {
        synchronized (lock) {
            if (items.containsKey(name)) {
                throw new ItemAlreadyExists();
            } else {
                ServantFactory factory = factories.get(type);
                if (factory == null) {
                    System.out.println("[Warning] Item [" + name + 
                            "] of unknown type `" + type + "' requested; " +
                            "using default");
                    factory = defaultFactor;
                }
                Item item = createItem(factory, name);
                ItemState state = new ItemState(item);
                items.put(name, state);
                state.take();
                System.out.println("Item [" + name + "] created");
                return state.getItem();
            }
        }
    }
    
    private Item createItem(ServantFactory factory, String name) {
        Servant servant = factory.create(name);
        try {
            byte[] id = poa.activate_object(servant);
            org.omg.CORBA.Object o = poa.id_to_reference(id);
            return ItemHelper.narrow(o);
        } catch (ServantAlreadyActive e) {
            System.err.println("Warning: servant for " + name + 
                    " has already been activated");
            throw new RuntimeException(e);
        } catch (WrongPolicy e) {
            System.err.println("Items POA is not configured correctly, " + 
                    "RETAIN policy should be specified");
            throw new RuntimeException(e);
        } catch (ObjectNotActive e) {
            throw new Error("Shouldn't happen");
        }
    }

    @Override
    public Item take_item(String name) throws ItemNotExists, ItemBusy {
        synchronized (lock) {
            ItemState state = items.get(name);
            if (state != null) {
                if (! state.take()) {
                    throw new ItemBusy();
                } else {
                    System.out.println("Item [" + name + "] taken");
                    return state.getItem();
                }
            } else {
                throw new ItemNotExists();
            }
        }
    }

    @Override
    public void release_item(String name) throws ItemNotExists {
        synchronized (lock) {
            ItemState state = items.get(name);
            if (state != null) {
                System.out.println("Item [" + name + "] released");
                state.release();
            } else {
                throw new ItemNotExists();
            }
        }
    }
    
    public void printItems() {
        synchronized (lock) {
            for (Entry<String, ItemState> pair: items.entrySet()) {
                Item item = pair.getValue().item;
                StringBuilder sb = new StringBuilder(pair.getKey());
                sb.append(": ")
                    .append(pair.getValue().taken ? "taken" : "free")
                    .append(" [").append(item.get_item_age())
                    .append("s]");
                System.out.println(sb);
            }
        }
    }

}
