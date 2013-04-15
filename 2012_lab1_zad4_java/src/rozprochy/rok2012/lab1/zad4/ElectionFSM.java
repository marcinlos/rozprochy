package rozprochy.rok2012.lab1.zad4;

import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import rozprochy.rok2012.lab1.zad4.cli.CommandHandler;
import rozprochy.rok2012.lab1.zad4.connection.Connection;
import rozprochy.rok2012.lab1.zad4.connection.Datagram;
import rozprochy.rok2012.lab1.zad4.connection.DatagramHandler;
import rozprochy.rok2012.lab1.zad4.connection.Parser;
import rozprochy.rok2012.lab1.zad4.fsm.AbstractState;
import rozprochy.rok2012.lab1.zad4.fsm.FSM;

public class ElectionFSM implements DatagramHandler<Datagram>, CommandHandler {

    private enum StateLabel {
        INACTIVE,
        INIT_SENT,
        LEADER,
        LEADER_ELECTED
    }
    
    private static final int TIMEOUT = 0; 
    private static final int TIMEOUT_REELECT = 1;
    private static final int GOT_OK = 2;
    private static final int GOT_INIT = 3;
    private static final int ACTIVATED = 4;
    private static final int GOT_WON = 5;
    private static final int WON = 6;
    
    public static final int OK_DELAY = 100;
    public static final int KEEPALIVE_DELAY = 1000;

    abstract class ElectionState extends AbstractState {
        public abstract void handle(byte type, Datagram datagram);
        public void shutdown() { }
    }
    
    private Connection connection;
    private long id;
    private String login;
    
    private long leaderId;
    private String leaderLogin;
    
    private boolean needsLeader = false;
    
    private FSM<ElectionState, StateLabel, Integer> fsm = 
            new FSM<ElectionState, StateLabel, Integer>();
    
    public ElectionFSM(final long id, final String login, 
            final Connection connection) {
        this.id = id;
        this.connection = connection;
        this.login = login;
        
        System.out.printf("[Election] ID: %x\n", id);
        
        fsm.addTransition(StateLabel.INACTIVE, ACTIVATED, StateLabel.INIT_SENT);
        fsm.addTransition(StateLabel.INACTIVE, GOT_WON, StateLabel.LEADER_ELECTED);
        fsm.addTransition(StateLabel.INACTIVE, GOT_INIT, StateLabel.INIT_SENT);
        
        fsm.addTransition(StateLabel.INIT_SENT, GOT_OK, StateLabel.INACTIVE);
        fsm.addTransition(StateLabel.INIT_SENT, GOT_WON, StateLabel.LEADER_ELECTED);
        fsm.addTransition(StateLabel.INIT_SENT, WON, StateLabel.LEADER);
        
        fsm.addTransition(StateLabel.LEADER_ELECTED, TIMEOUT, StateLabel.INACTIVE);
        fsm.addTransition(StateLabel.LEADER_ELECTED, TIMEOUT_REELECT, 
                StateLabel.INIT_SENT);
        
        fsm.addInitialState(StateLabel.INACTIVE, new ElectionState() {

            @Override 
            public void handle(byte type, Datagram datagram) {
                if (type == DatagramType.ELECTION_START) {
                    ElectionDatagram election = (ElectionDatagram) datagram;
                    long otherId = election.getId();
                    System.out.printf("[Election] Initiated by %s (%x)\n", 
                            election.getLogin(), otherId);
                    
                    if (otherId < id) {
                        connection.send(DatagramType.ELECTION_OK, datagram);
                        System.out.println("[Election] Sending OK");
                        fsm.advance(GOT_INIT);
                    } else if (otherId > id) {
                        System.out.println("[Election] Higher ID, ignoring");
                    }
                } else if (type == DatagramType.ELECTION_WON) {
                    ElectionDatagram election = (ElectionDatagram) datagram;
                    leaderId = election.getId();
                    leaderLogin = election.getLogin();
                    System.out.println("[Election] Leader elected: " + 
                            leaderLogin);
                    fsm.advance(GOT_WON);
                }
            }
            
        });
        
        fsm.addState(StateLabel.INIT_SENT, new ElectionState() {

            private Timer timeout;
            private volatile boolean finished;
            
            @Override 
            public void onBegin() {
                connection.send(DatagramType.ELECTION_START, makeDatagram());
                System.out.println("[Election] Election starts, waiting for OKs");
                finished = false;
                timeout = new Timer();
                timeout.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // UGLY
                        // Poor design, overlooked asynchronous nature of 
                        // timeouts. This is to prevent the race condition.
                        synchronized (fsm) {
                            if (! finished) {
                                System.out.println("[Election] No OK received");
                                ElectionDatagram datagram = makeDatagram();
                                connection.send(DatagramType.ELECTION_WON, datagram);
                                finished = true;
                                fsm.advance(WON);
                            }
                        }
                    }
                }, OK_DELAY);
            }
            
            @Override 
            public void handle(byte type, Datagram datagram) {
                if (type == DatagramType.ELECTION_OK) {
                    synchronized (fsm) {
                        if (! finished) {
                            ElectionDatagram election = (ElectionDatagram) datagram;
                            String target = election.getLogin();
                            if (target.equals(login)) {
                                System.out.println("[Election] Got OK");
                                finished = true;
                                timeout.cancel();
                                fsm.advance(GOT_OK);
                            } else {
                                System.out.println("I have OK for " + target);
                            }
                        }
                    }
                } else if (type == DatagramType.ELECTION_WON) {
                    synchronized (fsm) {
                        if (! finished) {
                            ElectionDatagram election = (ElectionDatagram) datagram;
                            leaderId = election.getId();
                            leaderLogin = election.getLogin();
                            System.out.println("[Election] Leader elected: " + 
                                    leaderLogin);
                            finished = true;
                            timeout.cancel();
                            fsm.advance(GOT_WON);
                        }
                    }
                } else if (type == DatagramType.ELECTION_START) {
                    ElectionDatagram election = (ElectionDatagram) datagram;
                    long otherId = election.getId();
                    System.out.printf("[Election] Initiated by %s (%x)\n", 
                            election.getLogin(), otherId);
                    
                    if (otherId < id) {
                        connection.send(DatagramType.ELECTION_OK, datagram);
                        System.out.println("[Election] Sending OK");
                    } else if (otherId > id) {
                        System.out.println("[Election] Higher ID, ignoring");
                    }
                }
            }
            
            @Override
            public void shutdown() {
                synchronized (fsm) {
                    finished = true;
                    timeout.cancel();
                }
            }
        });
        
        fsm.addState(StateLabel.LEADER, new ElectionState() {
            
            private Timer keepalive;
           
            @Override
            public void onBegin() {
                System.out.println("[Election] I am the leader");
                keepalive = new Timer();
                keepalive.scheduleAtFixedRate(new TimerTask() {
                    
                    @Override
                    public void run() {
                        ElectionDatagram datagram = makeDatagram();
                        System.out.println("[Election] Sending keepalive");
                        connection.send(DatagramType.LEADER_LIVES, datagram);
                    }
                }, KEEPALIVE_DELAY, KEEPALIVE_DELAY);
            }
            
            @Override
            public void handle(byte type, Datagram datagram) {
                if (type == DatagramType.ELECTION_START) {
                    // Leader is elected, someone doesn't know this and starts 
                    // a new election.
                    ElectionDatagram response = new ElectionDatagram(id, login);
                    connection.send(DatagramType.ELECTION_WON, response);
                } else {
                    printDiag(type, datagram);
                }
            }
            
            @Override
            public void shutdown() {
                keepalive.cancel();
            }
        });
        
        fsm.addState(StateLabel.LEADER_ELECTED, new ElectionState() {
            
            private Timer timeout;
            private volatile boolean alive;
            
            @Override
            public void onBegin() {
                alive = true;
                timeout = new Timer();
                timeout.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (! alive) {
                            System.out.println("[Election] Leader timed out");
                            timeout.cancel();
                            fsm.advance(needsLeader ? TIMEOUT_REELECT : TIMEOUT);
                        } else {
                            alive = false;
                        }
                    }
                }, 0, KEEPALIVE_DELAY * 5);
            }
            
            @Override
            public void handle(byte type, Datagram datagram) {
                if (type == DatagramType.LEADER_LIVES) {
                    ElectionDatagram election = (ElectionDatagram) datagram;
                    if (election.getId() == leaderId) {
                        System.out.println("[Election] Got keepalive");
                        alive = true;
                    } else {
                        System.out.println("[Election] New leader without " +
                                "exceeding the timeout: " + election.getLogin());
                    }
                }
            }
            
            @Override
            public void shutdown() {
                timeout.cancel();
            }
        });
    }
    
    @Override
    public void handle(byte type, Datagram datagram) {
        //printDiag(type, datagram);
        fsm.getCurrentState().handle(type, datagram);
    }

    public void initiateElection() {
        fsm.advance(ACTIVATED);
    }
    
    public void shutdown() {
        fsm.getCurrentState().shutdown();
    }
    
    private void printDiag(byte type, Datagram datagram) {
        switch (type) {
        case DatagramType.ELECTION_START:
            System.out.println("[Election msg START]");
            break;
            
        case DatagramType.ELECTION_OK:
            System.out.println("[Election msg OK]");
            break;
            
        case DatagramType.ELECTION_WON:
            System.out.println("[Election msg WON]");
            break;
        }
    }

    public static Parser<Datagram> getParser() {
        
        return new Parser<Datagram>() {
            @Override
            public Datagram parse(byte type, ByteBuffer buffer) {
                return ElectionDatagram.decode(buffer);
            }
        };
    }

    @Override
    public boolean handle(String args) {
        if (args == null) {
            args = "";
        }
        Scanner s = new Scanner(args);
        if (s.hasNext()) {
            String action = s.next();
            if (action.equals("auto")) {
                needsLeader = true;
            } else {
                System.out.println("Unknown command: `" + action + "'");
            }
        }
        else {
            if (fsm.getCurrentStateLabel() == StateLabel.INACTIVE) {
                System.out.println("Election");
                initiateElection();
            } else {
                System.out.println("Cannot initiate election");
            }
        }
        return true;
    }
    
    private ElectionDatagram makeDatagram() {
        return new ElectionDatagram(id, login);
    }

}
