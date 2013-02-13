package rozprochy.rok2011.lab2.zad1.server;

import java.io.PrintStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import rozprochy.rok2011.lab2.zad1.common.Compute;
import rozprochy.rok2011.lab2.zad1.common.Task;
import rozprochy.rok2011.lab2.zad1.common.TaskInfo;



public class ComputeEngine implements Compute {
    
    private final PrintStream log = System.err;
    
    private final static int HIST_SIZE = 10;
    
    private Deque<TaskInfo> history = new ArrayDeque<TaskInfo>(HIST_SIZE);
    
    
    public ComputeEngine() {
        super();
    }
    
    /**
     * Forwards to static method or {@link RemoteServer}
     * @return name of the client host, whose request is currently being
     * executed in this thread
     */
    private String getClientHost() {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            throw new Error("Something's really badly broken");
        }
    }
    
    /**
     * Adds a description to the internal queue in a thread-safe manner.
     */
    private void addToHistory(TaskInfo info) {
        synchronized (history) {
            if (history.size() >= HIST_SIZE) {
                history.pollLast();
            }
            history.offerFirst(info);
        }
    }

    public <T> T executeTask(Task<T> t) {
        String client = getClientHost();
        TaskInfo info = new TaskInfo(client, t.getClass().getName());
        addToHistory(info);
        boolean success = true;
        try {
            return t.execute();
        } catch (Exception e) {
            log.println("From " + client + ":");
            log.println("Exception during task execution: " + e.getMessage());
            e.printStackTrace(log);
            success = false;
            synchronized (info) {
                info.setException(e);
            }
            return null;
        } finally {
            synchronized (info) {
                info.finished(success);
            }
            log.println("Task for " + client + " has finished");
        }
    }


    /**
     * @return <= {@code n} most recently started tasks
     */
    @Override
    public List<TaskInfo> recentTasks(int n) {
        List<TaskInfo> tasks = new ArrayList<TaskInfo>();
        for (TaskInfo task : history) {
            if (n -- > 0) {
                tasks.add(task);
            } else {
                break;
            }
        }
        return null;
    }

    
    @Override
    public TaskInfo longestRunning() {
        TaskInfo longest = null;
        long time = 0;
        for (TaskInfo info : history) {
            if (info.hasSucceeded()) {
                long t = info.getRunningTime();
                if (t > time) {
                    time = t;
                    longest = info;
                }
            }
        }
        return longest;
    }
    
    
    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "Compute";
            Compute engine = new ComputeEngine();
            Compute stub =
                (Compute) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("ComputeEngine bound");
        } catch (Exception e) {
            System.err.println("ComputeEngine exception:");
            e.printStackTrace(System.err);
        }
    }
}
