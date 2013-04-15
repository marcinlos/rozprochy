package rozprochy.rok2012.lab1.zad4.connection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Dispatcher {

    private static class Entry<T> {
        
        final PacketFilter filter;
        final DatagramHandler<? super T> handler;
        final Parser<T> parser;
        
        Entry(PacketFilter filter, DatagramHandler<? super T> handler,
                Parser<T> parser) {
            this.filter = filter;
            this.handler = handler;
            this.parser = parser;
        }
    }
    
    private List<Entry<?>> entries = new ArrayList<Entry<?>>();

    public <T> void addHandler(PacketFilter filter, Parser<T> parser, 
            DatagramHandler<? super T> handler) {
        Entry<T> entry = new Entry<T>(filter, handler, parser);
        entries.add(entry);
    }
    
    public void dispatch(ByteBuffer buffer) 
            throws NoHandlerException {
        byte type = buffer.get();
        boolean handled = false;
        Iterator<Entry<?>> it = entries.iterator();
        while (! handled && it.hasNext()) {
            Entry<?> entry = it.next();
            if (entry.filter.matches(type)) {
                handleWith(entry, type, buffer);
                handled = true;
            }
        }
        if (! handled) {
            throw new NoHandlerException(type);
        }
    }
    
    private <T> void handleWith(Entry<T> entry, byte type, ByteBuffer buffer) {
        T datagram = entry.parser.parse(type, buffer);
        entry.handler.handle(type, datagram);
    }

}
