package rozprochy.lab2.server;

import rozprochy.lab2.common.GameListener;

public class Player {

    private String nick;
    private GameListener listener;
    
    public Player(String nick, GameListener listener) {
        this.nick = nick;
        this.listener = listener;
    }
    
    public String getNick() {
        return nick;
    }
    
    public GameListener getListener() {
        return listener;
    }

}
