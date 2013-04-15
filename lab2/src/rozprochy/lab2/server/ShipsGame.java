package rozprochy.lab2.server;

import java.rmi.RemoteException;
import java.util.Random;

import rozprochy.lab2.common.GameMove;
import rozprochy.lab2.common.GameResult;
import rozprochy.lab2.common.MoveResult;
import rozprochy.lab2.common.exceptions.JoinException;
import rozprochy.lab2.common.game.Board;
import rozprochy.lab2.common.game.BoardUpdate;
import rozprochy.lab2.common.game.EnemyShootUpdate;
import rozprochy.lab2.common.game.FieldState;
import rozprochy.lab2.common.game.ShootMove;
import rozprochy.lab2.common.game.ShootResult;

public class ShipsGame extends GameInstance {
    
    public static final int FIELDS = 7;
    
    private Board firstBoard = new Board();
    private Board secondBoard = new Board();
    
    
    public ShipsGame(long id, Player first) throws RemoteException {
        super(id, first);
        randomize(firstBoard);
        randomize(secondBoard);
        first.getListener().gameUpdated(new BoardUpdate(firstBoard));
    }
    
    @Override
    protected MoveResult handleMove(GameMove move) throws RemoteException {
        ShootMove shoot = (ShootMove) move;
        int x = shoot.point.x;
        int y = shoot.point.y;
        Board board = current == first ? secondBoard : firstBoard;
        boolean hit = shoot(board, x, y);
        EnemyShootUpdate update;
        ShootResult result;
        if (hit) {
            update = new EnemyShootUpdate(shoot.point, true);
            result = ShootResult.HIT;
        } else {
            update = new EnemyShootUpdate(shoot.point, false);
            result = ShootResult.MISS;
        }
        other.getListener().gameUpdated(update);
        if (allHit(board)) {
            GameResult res = new GameResult(current.getNick());
            first.getListener().gameEnded(res);
            second.getListener().gameEnded(res);
            finishGame();
        }
        return result;
    }
    
    @Override
    public synchronized void join(Player player) throws JoinException,
            RemoteException {
        super.join(player);
        player.getListener().gameUpdated(new BoardUpdate(secondBoard));
    }
    
    private void randomize(Board board) {
        Random rand = new Random();
        for (int i = 0; i < FIELDS; ) {
            int x = rand.nextInt(Board.SIZE);
            int y = rand.nextInt(Board.SIZE);
            int dir = rand.nextInt(4);
            int dx = (dir / 2 == 0 ? 1 : -1) * (dir % 2);
            int dy = (dir / 2 == 0 ? 1 : -1) * ((dir + 1) % 2);
            boolean ok = true;
            for (int j = 0; j < 3; ++ j) {
                int xx = x + j * dx;
                int yy = y + j * dy;
                if (! (xx < Board.SIZE && xx >= 0 && 
                       yy < Board.SIZE && yy >= 0 &&
                    (board.fields[yy][xx] & FieldState.SHIP) == 0)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                for (int j = 0; j < 3; ++ j) {
                    int xx = x + j * dx;
                    int yy = y + j * dy;
                    board.fields[yy][xx] |= FieldState.SHIP;
                }
                ++ i;
            }
        }
    }
    
    private boolean allHit(Board board) {
        for (int i = 0; i < Board.SIZE; ++ i) {
            for (int j = 0; j < Board.SIZE; ++ j) {
                if (board.fields[i][j] == FieldState.SHIP) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean shoot(Board board, int x, int y) {
        board.fields[y][x] |= FieldState.HIT;
        if ((board.fields[y][x] & FieldState.SHIP) != 0) {
            return true;
        } else {
            return false;
        }
    }

}
