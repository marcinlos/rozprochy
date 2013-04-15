package rozprochy.lab3.server.items;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ShortHolder;

import rozprochy.lab3.MiddlewareTestbed.ItemCOperations;

public class ItemCImpl extends AbstractItem implements ItemCOperations {

    public ItemCImpl(String name) {
        super(name);
    }

    @Override
    public void actionC(IntHolder a, ShortHolder b) {
        System.out.println("ItemC [" + name() + "] called with a=" + a.value);
        a.value = 666;
        b.value = 12;
    }

}
