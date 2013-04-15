package rozprochy.lab2.common.game;

import rozprochy.lab2.common.GameUpdate;

public class BoardUpdate implements GameUpdate {
    
    public final Board board;

    public BoardUpdate(Board board) {
        this.board = board;
    }

}
