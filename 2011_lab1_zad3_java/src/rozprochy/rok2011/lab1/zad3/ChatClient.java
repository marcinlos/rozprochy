package rozprochy.rok2011.lab1.zad3;

/**
 * Chat interface, to decouple PDU interpretation and low-level 
 * sending/receiving.
 */
public interface ChatClient {
    
    void gotDatagram(ChatDatagram data); 

}
