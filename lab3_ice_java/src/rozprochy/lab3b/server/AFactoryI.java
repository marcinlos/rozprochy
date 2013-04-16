package rozprochy.lab3b.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import rozprochy.lab3b.server.items.ItemAImpl;
import rozprochy.lab3b.server.items.ItemBImpl;
import rozprochy.lab3b.server.items.ItemCImpl;
import Ice.Current;
import Ice.Identity;
import Ice.ObjectPrx;
import MiddlewareTestbed.Item;
import MiddlewareTestbed.ItemAlreadyExists;
import MiddlewareTestbed.ItemBusy;
import MiddlewareTestbed.ItemNotExists;
import MiddlewareTestbed.ItemPrx;
import MiddlewareTestbed.ItemPrxHelper;
import MiddlewareTestbed._AFactoryDisp;
import MiddlewareTestbed._ItemAOperations;
import MiddlewareTestbed._ItemATie;
import MiddlewareTestbed._ItemBOperations;
import MiddlewareTestbed._ItemBTie;
import MiddlewareTestbed._ItemCOperations;
import MiddlewareTestbed._ItemCTie;
import MiddlewareTestbed._ItemOperations;

public class AFactoryI extends _AFactoryDisp {

    private static class ItemState {
        private boolean taken = false;
        private Item item;
        private Identity id;
        
        public ItemState(Item item, Identity id) {
            this.item = item;
            this.id = id;
        }
        
        public Identity getId() {
            return id;
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
    
    private Ice.ObjectAdapter adapter;
    private Map<String, ItemState> items = new HashMap<String, ItemState>();
    private final Object lock = new Object();
    
    private ServantFactory defaultFactory = new BrutalFactory(ItemAImpl.class, 
            _ItemAOperations.class, _ItemATie.class);
    private Map<String, ServantFactory> factories = 
            new HashMap<String, ServantFactory>();
    
    /*
     * Uses one-argument string constructor, if there is one 
     */
    private class BrutalFactory implements ServantFactory {
        
        private Constructor<? extends _ItemOperations>  itemCtor;
        private Constructor<? extends Item> servantCtor;

        public BrutalFactory(
                Class<? extends _ItemOperations> itemClazz,
                Class<? extends _ItemOperations> itemIface,
                Class<? extends Item> servantClazz) {
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
        public Item create(String name) {
            try {
                _ItemOperations obj = itemCtor.newInstance(name);
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

    public AFactoryI(Ice.ObjectAdapter adapter) {
        this.adapter = adapter;
        factories.put("ItemA", new BrutalFactory(ItemAImpl.class, 
                _ItemAOperations.class, _ItemATie.class));
        factories.put("ItemB", new BrutalFactory(ItemBImpl.class, 
                _ItemBOperations.class, _ItemBTie.class));
        factories.put("ItemC", new BrutalFactory(ItemCImpl.class, 
                _ItemCOperations.class, _ItemCTie.class));
    }

    @Override
    public ItemPrx createItem(String name, String type, Current __current)
            throws ItemAlreadyExists {
        synchronized (lock) {
            if (items.containsKey(name)) {
                throw new ItemAlreadyExists();
            } else {
                ServantFactory factory = factories.get(type);
                if (factory == null) {
                    System.out.println("[Warning] Item [" + name + 
                            "] of unknown type `" + type + "' requested; " +
                            "using default");
                    factory = defaultFactory;
                }
                Item item = factory.create(name);
                ObjectPrx objPrx = adapter.addWithUUID(item);
                System.out.println("ID: " + objPrx.ice_getIdentity());
                ItemState state = new ItemState(item, objPrx.ice_getIdentity());
                System.out.println("Szit: " + adapter.find(state.getId()));
                items.put(name, state);
                state.take();
                System.out.println("Item [" + name + "] created");
                return ItemPrxHelper.checkedCast(objPrx);
            }
        }
    }
    
    @Override
    public ItemPrx takeItem(String name, Current __current) throws ItemBusy,
            ItemNotExists {
        synchronized (lock) {
            ItemState state = items.get(name);
            if (state != null) {
                if (! state.take()) {
                    throw new ItemBusy();
                } else {
                    System.out.println("Item [" + name + "] taken");
                    ObjectPrx objPrx = adapter.createProxy(state.getId());
                    return ItemPrxHelper.checkedCast(objPrx);
                }
            } else {
                throw new ItemNotExists();
            }
        }
    }

    @Override
    public void releaseItem(String name, Current __current)
            throws ItemNotExists {
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
                    .append(" [").append(item.getItemAge())
                    .append("s]");
                System.out.println(sb);
            }
        }
    }

}
