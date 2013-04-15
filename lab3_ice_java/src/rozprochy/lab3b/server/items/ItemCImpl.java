package rozprochy.lab3b.server.items;

import Ice.Current;
import Ice.IntHolder;
import Ice.ShortHolder;
import MiddlewareTestbed._ItemCOperations;

public class ItemCImpl extends AbstractItem implements _ItemCOperations {

    public ItemCImpl(String name) {
        super(name);
    }

    @Override
    public void actionC(int a, IntHolder aOut, ShortHolder b,
            Current __current) {
        System.out.println("ItemC [" + name() + "] called with a=" + a);
        aOut.value = 666;
        b.value = 12;
    }

}
