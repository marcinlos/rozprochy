package rozprochy.rok2011.lab2.zad1.common;

import java.io.Serializable;

/**
 * Class representing information about origin and state of some
 * task
 */
public class TaskInfo implements Serializable {
    
    /** Address of a client who requested a call */
    private String clientHost;
    
    /** Name of a task's class */
    private String taskClassName;
    
    /** Short task description, as returned by task's {@code toString} */
    private String description;
    
    /** Whether or not the invoked method is still being executed */
    private boolean running;
    
    /** Whetehr or not the invocation has succeeded */
    private boolean succeeded;
    
    /** Exception thrown by the call, or {@code null} if there was none */
    private Throwable exception;

    /** Time measurement - in nanoseconds */
    private long execBegin;
    private long execEnd;
    
    
    public TaskInfo(String client, String className, String description) {
        this.clientHost = client;
        this.taskClassName = className;
        this.description = description;
        this.running = true;
        this.succeeded = false;
        this.execBegin = System.nanoTime();
    }


    public String getClientHost() {
        return clientHost;
    }


    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }


    public String getTaskClassName() {
        return taskClassName;
    }


    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
    }
    
    
    public String getDescription() {
        return description;
    }


    public boolean isRunning() {
        return running;
    }


    public boolean hasSucceeded() {
        return succeeded || isRunning();
    }


    /**
     * Invoked after the task finishes
     */
    public void finished(boolean succeeded) {
        if (! isRunning()) {
            throw new IllegalStateException("Task has already finished once");
        }
        this.succeeded = succeeded;
        running = false;
        execEnd = System.nanoTime();
    }


    public Throwable getException() {
        return exception;
    }


    public void setException(Throwable exception) {
        this.exception = exception;
    }
    
    /**
     * @return amount of nanoseconds the task was running (keep in mind the 
     * actual precision may well be lower)
     */
    public long getRunningTime() {
        if (running) {
            throw new IllegalStateException("Task is still running");
        } 
        return execEnd - execBegin;
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Host: ").append(getClientHost()).append('\n')
        .append("Class: ").append(getTaskClassName()).append('\n')
        .append("Task: ").append(description).append('\n')
        .append("State: ");
        if (isRunning()) {
            sb.append("running");
        } else if (hasSucceeded()) {
            sb.append("done");
        } else {
            String msg = getException().getMessage();
            sb.append("failed (").append(msg).append(')');
        }
        sb.append('\n');
        if (! isRunning()) {
            sb.append("Time: ").append(getRunningTime() / 1e6).append("ms");
        }
        return sb.toString();
    }

}
