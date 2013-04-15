package rozprochy.lab3.server.items;

import rozprochy.lab3.MiddlewareTestbed.ItemOperations;

public class AbstractItem implements ItemOperations {

    private String name;
    private long begin;
    
    public AbstractItem(String name) {
        this.name = name;
        this.begin = System.currentTimeMillis();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int get_item_age() {
        long now = System.currentTimeMillis();
        return (int)((now - begin) / 1000);
    }

}
