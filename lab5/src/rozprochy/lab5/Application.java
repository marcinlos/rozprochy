package rozprochy.lab5;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import rozprochy.lab5.cli.Command;
import rozprochy.lab5.cli.CommandInterpreter;

public class Application {
    
    private Client client;
    private String defaultChannel;
    private CommandInterpreter cli;
    
    public Application(Client client) throws IOException {
        this.client = client;
        this.cli = new CommandInterpreter();
        setupActions();
    }
    
    public void run() throws IOException {
        cli.setPrompt(client.getNick() + "> ");
        cli.run();
        client.close();
        System.out.print("\r");
    }

    private void setupActions() {
        cli.registerHandler("/join", new Command() {
            @Override
            public boolean execute(String cmd, Scanner input) {
                try {
                    String channel = input.next();
                    client.joinChannel(channel);
                    defaultChannel = channel;
                } catch (NoSuchElementException e) {
                    System.err.println("Usage: /join <channel>");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        });
        cli.registerHandler("/leave", new Command() {
            @Override
            public boolean execute(String cmd, Scanner input) {
                try {
                    String channel = input.next();
                    client.leaveChannel(channel);
                } catch (NoSuchElementException e) {
                    System.err.println("Usage: /leave <channel>");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        });
        cli.registerHandler("/channels", new Command() {
            @Override
            public boolean execute(String cmd, Scanner input) {
                Map<String, Set<String>> ch = client.getMembership();
                for (String name: ch.keySet()) {
                    System.out.println(" *  " + name);
                }
                return true;
            }
        });
        cli.registerHandler("/users", new Command() {
            @Override
            public boolean execute(String cmd, Scanner input) {
                Map<String, Set<String>> ch = client.getMembership();
                for (Entry<String, Set<String>> channel: ch.entrySet()) {
                    System.out.println("  " + channel.getKey());
                    for (String user: channel.getValue()) {
                        System.out.println("  - " + user);
                    }
                }
                return true;
            }
        });
        cli.registerHandler("/set", new Command() {
            @Override
            public boolean execute(String cmd, Scanner input) {
                try {
                    String channel = input.next();
                    Map<String, Set<String>> ch = client.getMembership();
                    if (!ch.containsKey(channel)) {
                        System.err.println("Room does not exist!");
                    }
                    Set<String> joined = client.getChannels();
                    if (! joined.contains(channel)) {
                        System.err.println("Join the channel first");
                    }
                } catch (NoSuchElementException e) {
                    System.err.println("Usage: /set <channel>");
                }
                return true;
            }
        });
        cli.setDefaultHandler(new Command() {
            @Override
            public boolean execute(String cmd, Scanner input) {
                if (defaultChannel == null) {
                    System.err.println("Channel not set!");
                } else {
                }
                return true;
            }
        });
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: chat <nick>");
            System.exit(1);
        }
        try {
            Client client = new Client(args[0]);
            Application app = new Application(client);
            app.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
