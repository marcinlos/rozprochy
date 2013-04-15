package rozprochy.lab2.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import rozprochy.lab2.common.Game;
import rozprochy.lab2.common.GameListener;
import rozprochy.lab2.common.GameMove;
import rozprochy.lab2.common.MoveResult;
import rozprochy.lab2.common.Session;
import rozprochy.lab2.common.exceptions.JoinException;
import rozprochy.lab2.common.exceptions.MoveException;

public class SessionImpl extends UnicastRemoteObject implements Session {

    private String nick;
    private GameServerImpl server;

    public SessionImpl(String nick, GameServerImpl server)
            throws RemoteException {
        this.nick = nick;
        this.server = server;
        System.out.println("Session exported");
    }

    @Override
    public Game join(String room, long id, GameListener listener)
            throws RemoteException, JoinException {
        Player player = new Player(nick, listener);
        GameInstance game = server.join(room, id, player);
        Game forwarder = new Forwarder(game);
        game.setSecondGame(forwarder);
        return forwarder;
    }

    @Override
    public void logout() throws RemoteException {
        server.logout(nick);
    }

    @Override
    public Game create(String room, GameListener listener)
            throws RemoteException {
        Player player = new Player(nick, listener);
        GameInstance game = server.create(room, player);
        Game forwarder = new Forwarder(game);
        game.setFirstGame(forwarder);
        return forwarder;
    }

    @Override
    public Game createWithBot(GameListener listener) throws RemoteException {
        Player player = new Player(nick, listener);
        GameInstance game = server.create("[priv]", player);
        Game forwarder = new Forwarder(game);
        game.setFirstGame(forwarder);
        BotListener botListener = new BotListener();
        String botNick = BotNames.randomName();
        botListener.setGame(new LocalForwarder(game, botNick));
        Player bot = new Player(botNick, botListener);
        try { 
            game.join(bot);
        } catch (JoinException e) {
            throw new Error(e);
        }
        return forwarder;
    }
    
    public void shutdown() {
        
    }
    
    private class Forwarder extends UnicastRemoteObject implements Game {

        private GameInstance game;
        
        protected Forwarder(GameInstance game) throws RemoteException {
            this.game = game;
            System.out.println("Forwarder exported");
        }

        @Override
        public MoveResult move(GameMove move) throws RemoteException,
                MoveException {
            return game.move(nick, move);
        }

        @Override
        public void leave() throws RemoteException {
            game.leave(nick);
        }
        
    }
    
    private class LocalForwarder implements Game {
        
        private GameInstance game;
        private String nick;
        
        protected LocalForwarder(GameInstance game, String nick) {
            this.game = game;
            this.nick = nick;
            System.out.println("Forwarder exported");
        }

        @Override
        public MoveResult move(GameMove move) throws RemoteException,
                MoveException {
            return game.move(nick, move);
        }

        @Override
        public void leave() throws RemoteException {
        }
    }

}
