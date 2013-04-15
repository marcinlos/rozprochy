package rozprochy.rok2012.lab1.zad4;


public interface DatagramType {

    public static final byte MESSAGE = 1;

    // 3rd bit set
    public static final byte ELECTION_START = 4;
    public static final byte ELECTION_OK    = 5;
    public static final byte ELECTION_WON   = 6;
    public static final byte LEADER_LIVES   = 7;
    
}
