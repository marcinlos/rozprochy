package rozprochy.lab3b.server.items;

import Ice.Current;
import Ice.LongHolder;
import MiddlewareTestbed._ItemAOperations;

public class ItemAImpl extends AbstractItem implements _ItemAOperations {

    public ItemAImpl(String name) {
        super(name);
    }

    @Override
    public void actionA(float a, LongHolder b, Current __current) {
        System.out.println("ItemA [" + name() + "] called with a=" + a);
        b.value = 666;
    }

}
