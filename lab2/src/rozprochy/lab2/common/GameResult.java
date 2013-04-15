package rozprochy.lab2.common;

import java.io.Serializable;

public class GameResult implements Serializable {
    
    private String winner;
    private String cause;

    public GameResult(String winner) {
        this(winner, null);
    }
    
    public GameResult(String winner, String cause) {
        this.winner = winner;
        this.cause = cause;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public String getCause() {
        return cause;
    }

}
