package rozprochy.rok2012.lab1.zad4.connection;

public class NoHandlerException extends Exception {

    private byte type;
    
    public NoHandlerException(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

}
