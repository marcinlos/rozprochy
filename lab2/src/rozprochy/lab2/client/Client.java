package rozprochy.lab2.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.Scanner;

import rozprochy.lab2.common.Game;
import rozprochy.lab2.common.GameListener;
import rozprochy.lab2.common.GameResult;
import rozprochy.lab2.common.GameServer;
import rozprochy.lab2.common.GameStatus;
import rozprochy.lab2.common.GameUpdate;
import rozprochy.lab2.common.MoveResult;
import rozprochy.lab2.common.Session;
import rozprochy.lab2.common.exceptions.GameFullException;
import rozprochy.lab2.common.exceptions.JoinException;
import rozprochy.lab2.common.exceptions.MoveException;
import rozprochy.lab2.common.exceptions.MoveOrderException;
import rozprochy.lab2.common.exceptions.NoSuchGameException;
import rozprochy.lab2.common.game.Board;
import rozprochy.lab2.common.game.BoardUpdate;
import rozprochy.lab2.common.game.EnemyShootUpdate;
import rozprochy.lab2.common.game.FieldState;
import rozprochy.lab2.common.game.Point;
import rozprochy.lab2.common.game.ShootMove;
import rozprochy.lab2.common.game.ShootResult;

class Client {
    
    private enum State {
        IN_MENU,
        IN_GAME
    }
    
    private static final String CONSOLE_RESET = "\u001B[0m";
    private static final String CONSOLE_RED   = "\u001B[1;41m";
    private static final String CONSOLE_WHITE   = "\u001B[1;47m";
    private static final String CONSOLE_GREEN   = "\u001B[1;42m";
    private static final String CONSOLE_YELLOW   = "\u001B[1;43m";
    
    
    private Scanner input = new Scanner(System.in);
    private GameServer server;
    private Session session;
    private State state;

    private Game game;
    private Board ourBoard;
    private Board enemyBoard;
    
    private GameListener listener;

    public Client(GameServer server, Session session) throws RemoteException {
        this.server = server;
        this.session = session;
        this.state = State.IN_MENU;
        this.listener = new Listener();
        System.out.println("Connection established");
    }
    
    private void printMenu() {
        System.out.println("Type 'help' to see the list of available actions");
    }
    
    public void run() throws RemoteException {
        try {
            while (input.hasNextLine()) {
                String line = input.nextLine();
                if (!interpret(line)) {
                    break;
                }
            }
        } finally {
            shutdown();
        }
    }

    private void shutdown() throws RemoteException {
        session.logout();
        if (listener != null) {
            UnicastRemoteObject.unexportObject(listener, true);
        }
    }
    
    private void printInstructions() {
        System.out.println(
        "  help           Displays description of all the commands\n" +
        "  games          Displays list of all the games currently played\n" +
        "  join <id>      Joins the game with given id\n" +
        "  create [bot]   Creates new game - optionally with the bot\n" +
        "  logout         Leaves the game\n"
        );
    }
    
    private void showGames() throws RemoteException {
        Collection<GameStatus> games = server.getGames();
        System.out.printf("%6s | %15s | %15s\n", "ID", "Player 1", "Player 2");
        System.out.println("-------------------------------------------");
        for (GameStatus game: games) {
            String p1 = game.getPlayer1() != null ? game.getPlayer1() : "-";
            String p2 = game.getPlayer2() != null ? game.getPlayer2() : "-";
            System.out.printf("%6d | %15s : %15s\n", game.getId(), p1, p2);
        }
        System.out.println("-------------------------------------------");
    }
    
    private void printSpace(int n) {
        for (int i = 0; i < n; ++ i) {
            System.out.print(' ');
        }
    }
    
    private void printHeader(int pad, int space) {
        printSpace(pad + 1);
        for (int i = 0; i < Board.SIZE; ++ i) {
            System.out.print((char)('a' + i) + " ");
        }
        printSpace(2 + space);
        for (int i = 0; i < Board.SIZE; ++ i) {
            System.out.print((char)('a' + i) + " ");
        }
        System.out.println();
    }
    
    private void printRow(int row, Board left, Board right, int pad, int space) {
        printSpace(pad);
        char digit = (char)('0' + row);
        System.out.print(digit);
        for (int j = 0; j < Board.SIZE; ++ j) {
            printField(left.fields[row][j]);
        }
        System.out.print(digit);
        printSpace(space);
        System.out.print(digit);
        for (int j = 0; j < Board.SIZE; ++ j) {
            printField(right.fields[row][j]);
        }
        System.out.print(digit);
        System.out.println();
    }
    
    private void printBoard(Board left, Board right) {
        final int PAD = 15;
        final int SPACE = 10;
        printHeader(PAD, SPACE);
        for (int i = 0; i < Board.SIZE; ++ i) {
            printRow(Board.SIZE - i - 1, left, right, PAD, SPACE);
        }
        printHeader(PAD, SPACE);
    }
    
    private void printField(int field) {
        String color;
        if ((field & FieldState.SHIP) != 0) {
            if ((field & FieldState.HIT) != 0) {
                color = CONSOLE_RED;
            } else {
                color = CONSOLE_GREEN;
            }
        } else {
            if ((field & FieldState.HIT) != 0) {
                color = CONSOLE_YELLOW;
            } else {
                color = CONSOLE_WHITE;
            }
        }
        System.out.print(color);
        System.out.print("  ");
        System.out.print(CONSOLE_RESET);
    }
    
    private void createGame() throws RemoteException {
        game = session.create("?", listener);
        state = State.IN_GAME;
    }
    
    
    private void createBotGame() throws RemoteException {
        game = session.createWithBot(listener);
        state = State.IN_GAME;
    }
    
    private void joinGame(long id) throws RemoteException {
        try {
            game = session.join("?", id, listener);
            state = State.IN_GAME;
        } catch (NoSuchGameException e) {
            System.err.println("Game [" + id + "] does not exist");
        } catch (GameFullException e) {
            System.err.println("Game [" + id + "] is full, sorry");
        } catch (JoinException e) {
            e.printStackTrace();
        }
    }
    
    
    private void makeMove(String move) throws RemoteException {
        try {
            char cx = move.charAt(0);
            char cy = move.charAt(1);
            int x = cx - 'a';
            int y = cy - '0';
            if (x < 0 || x >= Board.SIZE) {
                System.out.println("Invalid horizontal component: " + cx);
                return;
            }
            if (y < 0 || y >= Board.SIZE) {
                System.out.println("Invalid vertical component: " + cy);
                return;
            }
            MoveResult generic = game.move(new ShootMove(new Point(x, y)));
            ShootResult res = (ShootResult) generic;
            if (res.hit) {
                enemyBoard.fields[y][x] |= FieldState.SHIP;
            }
            enemyBoard.fields[y][x] |= FieldState.HIT; 
            printBoard(ourBoard, enemyBoard);
        } catch (MoveOrderException e) {
            System.out.println("Not your move");
        } catch (MoveException e) {
            e.printStackTrace();
        }
    }

    private boolean interpret(String line) throws RemoteException {
        Scanner s = new Scanner(line);
        if (! s.hasNext()) {
            return true;
        }
        String cmd = s.next();
        if (cmd.equals("help")) {
            printInstructions();
        } else if (state == State.IN_MENU) {
            if (cmd.equals("games")) {
                showGames();
            } else if (cmd.equals("logout")) {
                return false;
            } else if (cmd.equals("create")) {
                if (s.hasNext() && s.next().equals("bot")) {
                    createBotGame();
                } else {
                    createGame();
                }
            } else if (cmd.equals("join")) {
                if (s.hasNext()) {
                    try {
                        long id = s.nextLong();
                        joinGame(id);
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid game ID");
                    }
                } else {
                    System.out.println("Usage: join <id>");
                }
            } else {
                System.err.println("Unknown command: " + cmd);
            }
        } else if (state == State.IN_GAME) {
            if (cmd.equals("leave")) {
                game.leave();
                state = State.IN_MENU;
                System.out.println("Game left");
                printMenu();
            } else {
                makeMove(cmd);
            }
        }
        return true;
    }


    private class Listener extends UnicastRemoteObject implements GameListener {

        protected Listener() throws RemoteException {
        }

        @Override
        public void otherJoined(String nick) throws RemoteException {
            System.out.println("User [" + nick + "] joined the game");
        }

        @Override
        public void gameUpdated(GameUpdate update) throws RemoteException {
            if (update instanceof BoardUpdate) {
                BoardUpdate bupd = (BoardUpdate) update;
                ourBoard = bupd.board;
                enemyBoard = new Board();
            } else if (update instanceof EnemyShootUpdate) {
                EnemyShootUpdate u = (EnemyShootUpdate) update;
                System.out.println("Shoot: " + u.point + " " + 
                        (u.result ? "HIT" : "MISS"));
                ourBoard.fields[u.point.y][u.point.x] |= FieldState.HIT;
            }
            printBoard(ourBoard, enemyBoard);
        }

        @Override
        public void gameEnded(GameResult result) throws RemoteException {
            System.out.println("GAME OVER");
            System.out.println("WINNER: " + result.getWinner());
            if (result.getCause() != null) {
                System.out.println(result.getCause());
            }
            state = State.IN_MENU;
            printMenu();
        }
        
    }

}
