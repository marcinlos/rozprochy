package rozprochy.lab3.server.items;

import org.omg.CORBA.IntHolder;

import rozprochy.lab3.MiddlewareTestbed.ItemAOperations;

public class ItemAImpl extends AbstractItem implements ItemAOperations  {
    
    public ItemAImpl(String name) {
        super(name);
    }

    @Override
    public void actionA(float a, IntHolder b) {
        System.out.println("ItemA [" + name() + "] called with a=" + a);
        b.value = 666;
    }

}
