package rozprochy.lab3b.server.items;

import Ice.Current;
import MiddlewareTestbed._ItemDisp;


public class AbstractItem extends _ItemDisp {

    private String name;
    private long begin;
    
    public AbstractItem(String name) {
        this.name = name;
        this.begin = System.currentTimeMillis();
    }

    @Override
    public String name(Current __current) {
        return name;
    }

    @Override
    public int getItemAge(Current __current) {
        long now = System.currentTimeMillis();
        return (int)((now - begin) / 1000);
    }

}
