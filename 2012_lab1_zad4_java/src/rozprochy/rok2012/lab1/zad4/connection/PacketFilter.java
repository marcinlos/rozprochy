package rozprochy.rok2012.lab1.zad4.connection;

public interface PacketFilter {

    boolean matches(byte type);
    
}
