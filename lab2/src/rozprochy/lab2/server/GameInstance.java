package rozprochy.lab2.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import rozprochy.lab2.common.Game;
import rozprochy.lab2.common.GameMove;
import rozprochy.lab2.common.GameResult;
import rozprochy.lab2.common.MoveResult;
import rozprochy.lab2.common.exceptions.GameFullException;
import rozprochy.lab2.common.exceptions.JoinException;
import rozprochy.lab2.common.exceptions.MoveOrderException;

public abstract class GameInstance {

    protected GameState state = GameState.WAITING_FOR_PARTNER;
    protected GameServerImpl server;
    private long id;

    protected Player first;
    protected Player second;
    private Game firstGame;
    private Game secondGame;

    protected Player current;
    protected Player other;

    public GameInstance(long id, Player first) {
        this.id = id;
        this.first = first;
        this.current = first;
    }

    public long getId() {
        return id;
    }

    public Player getFirst() {
        return first;
    }

    public Player getSecond() {
        return second;
    }
    
    public void setFirstGame(Game game) {
        this.firstGame = game;
    }
    
    public void setSecondGame(Game game) {
        this.secondGame = game;
    }
    
    public void setServer(GameServerImpl server) {
        this.server = server;
    }

    public synchronized void join(Player player) throws JoinException,
            RemoteException {
        if (state == GameState.WAITING_FOR_PARTNER) {
            second = other = player;
            state = GameState.PLAYING;
            first.getListener().otherJoined(player.getNick());
        } else {
            throw new GameFullException();
        }
    }

    public synchronized MoveResult move(String player, GameMove move)
            throws MoveOrderException, RemoteException {
        if (state == GameState.PLAYING) {
            if (!current.getNick().equals(player)) {
                throw new MoveOrderException("It's " + current.getNick() + "'s move");
            }
            MoveResult result = handleMove(move);
            swapRoles();
            return result;
        } else {
            throw new MoveOrderException("Waiting for the opponent...");
        }
    }

    protected void swapRoles() {
        Player tmp = current;
        current = other;
        other = tmp;
    }

    protected abstract MoveResult handleMove(GameMove move)
            throws RemoteException;

    protected void finishGame() {
        server.removeGame(id);
        shutdown();
    }
    
    public void leave(String player) throws RemoteException {
        Player o = current.getNick().equals(player) ? other : current;
        if (o != null) {
            GameResult result = new GameResult(o.getNick(), 
                    "Opponent left the game");
            o.getListener().gameEnded(result);
        }
        finishGame();
    }

    public void shutdown() {
        try {
            if (firstGame != null) {
                System.out.println("Game unexported");
                UnicastRemoteObject.unexportObject(firstGame, true);
            }
            if (secondGame != null) {
                System.out.println("Game unexported");
                UnicastRemoteObject.unexportObject(secondGame, true);
            }
        } catch (NoSuchObjectException e) {
            throw new Error("Shouldn't happen");
        }
    }

}
