package rozprochy.rok2012.lab1.zad4.connection;

public class ExactFilter implements PacketFilter {

    private byte type;
    
    public ExactFilter(byte type) {
        this.type = type;
    }

    @Override
    public boolean matches(byte type) {
        return type == this.type;
    }

}
