package rozprochy.rok2012.lab1.zad4.connection;

public class AnyBitFilter implements PacketFilter {

    private byte mask;
    
    public AnyBitFilter(byte mask) {
        this.mask = mask;
    }
    
    @Override
    public boolean matches(byte type) {
        return (mask & type) != 0;
    }

}
