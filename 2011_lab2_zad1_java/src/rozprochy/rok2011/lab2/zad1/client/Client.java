package rozprochy.rok2011.lab2.zad1.client;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rozprochy.rok2011.lab2.zad1.client.rmi.Failure;
import rozprochy.rok2011.lab2.zad1.client.rmi.LongTask;
import rozprochy.rok2011.lab2.zad1.client.rmi.Pi;
import rozprochy.rok2011.lab2.zad1.common.Compute;
import rozprochy.rok2011.lab2.zad1.common.TaskInfo;


public class Client {
    
    public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "Compute";
            Registry registry = LocateRegistry.getRegistry(args[0]);
            Compute comp = (Compute) registry.lookup(name);
            
            if (args[1].equals("list")) {
                List<TaskInfo> tasks = comp.recentTasks(5);
                System.out.println("Recent tasks:");
                for (TaskInfo task : tasks) {
                    System.out.println(task);
                    System.out.println();
                }
            } else if (args[1].equals("longest")) {
                TaskInfo longest = comp.longestRunning();
                System.out.println("Longest running task:");
                System.out.println(longest);
            } else if (args[1].equals("fail")) {
                Failure task = new Failure();
                comp.executeTask(task);
            } else if (args[1].equals("long")) {
                int millis = Integer.parseInt(args[2]);
                LongTask task = new LongTask(millis, TimeUnit.MILLISECONDS);
                String result = comp.executeTask(task);
                System.out.println(result);
            } else if (args[1].equals("pi")) {
                Pi task = new Pi(Integer.parseInt(args[2]));
                BigDecimal pi = comp.executeTask(task);
                System.out.println(pi);
            }
        } catch (Exception e) {
            System.err.println("ComputePi exception:");
            e.printStackTrace(); 
        }
    }    
}