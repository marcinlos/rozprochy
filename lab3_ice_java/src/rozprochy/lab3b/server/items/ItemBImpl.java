package rozprochy.lab3b.server.items;

import Ice.Current;
import MiddlewareTestbed._ItemBOperations;

public class ItemBImpl extends AbstractItem implements _ItemBOperations {

    public ItemBImpl(String name) {
        super(name);
    }

    @Override
    public float actionB(String a, Current __current) {
        System.out.println("ItemB [" + name() + "] called with a='" + a + "'");
        return 3.141592658979f;
    }

}
