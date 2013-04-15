package rozprochy.lab3.server.items;

import rozprochy.lab3.MiddlewareTestbed.ItemBOperations;

public class ItemBImpl extends AbstractItem implements ItemBOperations {

    public ItemBImpl(String name) {
        super(name);
    }

    @Override
    public float actionB(String a) {
        System.out.println("ItemB [" + name() + "] called with a='" + a + "'");
        return 3.141592658979f;
    }

}
