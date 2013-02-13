package rozprochy.rok2011.lab2.zad1.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Compute extends Remote {
    
    /**
     * Executes the task
     */
    <T> T executeTask(Task<T> t) throws RemoteException;
    
    /**
     * Returns information about recently executed tasks
     * 
     * @param n maximal number of tasks to return
     * @return list of task description
     */
    List<TaskInfo> recentTasks(int n) throws RemoteException;
    
    /**
     * @return information about longest running finished task which 
     * is still present in history records
     */
    TaskInfo longestRunning() throws RemoteException;
    
}

