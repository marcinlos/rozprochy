package rozprochy.lab2.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rozprochy.lab2.common.GameServer;
import rozprochy.lab2.common.GameStatus;
import rozprochy.lab2.common.Session;
import rozprochy.lab2.common.exceptions.JoinException;
import rozprochy.lab2.common.exceptions.LoginException;
import rozprochy.lab2.common.exceptions.NoSuchGameException;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {

    private Map<String, SessionImpl> logged = new HashMap<String, SessionImpl>();
    private Map<Long, GameInstance> games = new HashMap<Long, GameInstance>();
    private long counter = 1;

    public GameServerImpl() throws RemoteException {
        System.out.println("Server exported");
    }

    @Override
    public Session login(String nick) throws RemoteException, LoginException {
        synchronized (logged) {
            if (logged.containsKey(nick)) {
                throw new LoginException("User already logged in");
            }
            SessionImpl session = new SessionImpl(nick, this);
            logged.put(nick, session);
            System.out.println("User [" + nick + "] logged in");
            return session;
        }
    }

    public void logout(String nick) {
        synchronized (logged) {
            SessionImpl session = logged.remove(nick);
            if (session != null) {
                endSession(session);
                System.out.println("User [" + nick + "] logged out");
            }
        }
    }

    public GameInstance create(String room, Player creator)
            throws RemoteException {
        synchronized (games) {
            long id = counter++;
            GameInstance game = new ShipsGame(id, creator);
            game.setServer(this);
            games.put(id, game);
            System.out.println("User [" + creator.getNick() + "] created "
                    + "game " + id + " in " + room);
            return game;
        }
    }

    public GameInstance join(String room, long id, Player player)
            throws JoinException, RemoteException {
        GameInstance game = null;
        synchronized (games) {
            game = games.get(id);
            if (game != null) {
                game.join(player);
                System.out.println("User [" + player.getNick() + "] joined " +
                        "game " + id + "@" + room);
                return game;
            } else {
                throw new NoSuchGameException();
            }
        }
    }

    @Override
    public Collection<GameStatus> getGames() throws RemoteException {
        List<GameStatus> gameList = new ArrayList<GameStatus>();
        synchronized (games) {
            for (GameInstance g: games.values()) {
                String first = g.getFirst().getNick();
                Player sndPlayer = g.getSecond();
                String second = sndPlayer == null ? null : sndPlayer.getNick();
                gameList.add(new GameStatus(g.getId(), first, second));
            }
        }
        return gameList;
    }
    
    public void removeGame(long id) {
        synchronized (games) {
            games.remove(id);
        }
    }
    
    public void shutdown() {
        synchronized (logged) {
            for (SessionImpl session: logged.values()) {
                endSession(session);
            }
        }
        synchronized (games) {
            for (GameInstance game: games.values()) {
                game.shutdown();
            }
        }
    }
    
    private void endSession(SessionImpl session) {
        try {
            session.shutdown();
            UnicastRemoteObject.unexportObject(session, true);
            System.out.println("Session unexported");
        } catch (NoSuchObjectException e) {
            throw new Error("Shouldn't happen");
        }
    }

}
