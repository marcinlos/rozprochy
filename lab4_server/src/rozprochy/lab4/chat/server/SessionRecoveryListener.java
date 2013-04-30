package rozprochy.lab4.chat.server;


public interface SessionRecoveryListener {

    void sessionRecovered(BiSession session);

}
