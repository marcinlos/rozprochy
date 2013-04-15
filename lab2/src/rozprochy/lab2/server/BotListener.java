package rozprochy.lab2.server;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import rozprochy.lab2.common.Game;
import rozprochy.lab2.common.GameListener;
import rozprochy.lab2.common.GameResult;
import rozprochy.lab2.common.GameUpdate;
import rozprochy.lab2.common.exceptions.MoveException;
import rozprochy.lab2.common.game.Board;
import rozprochy.lab2.common.game.Point;
import rozprochy.lab2.common.game.ShootMove;

public class BotListener implements GameListener {
    
    private Game game;
    private Random rand = new Random();
    private boolean running = false;

    public BotListener() {
        // TODO Auto-generated constructor stub
    }
    
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void otherJoined(String nick) throws RemoteException { }

    @Override
    public void gameUpdated(GameUpdate update) throws RemoteException {
        if (running) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        int x = rand.nextInt(Board.SIZE);
                        int y = rand.nextInt(Board.SIZE);
                        game.move(new ShootMove(new Point(x, y)));
                    } catch (MoveException e) {
                        throw new RuntimeException(e);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 1000);
        } else {
            running = true;
        }
    }

    @Override
    public void gameEnded(GameResult result) throws RemoteException { }

}
