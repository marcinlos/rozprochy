package rozprochy.lab5;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.protocols.BARRIER;
import org.jgroups.protocols.FD_ALL;
import org.jgroups.protocols.FD_SOCK;
import org.jgroups.protocols.FRAG2;
import org.jgroups.protocols.MERGE2;
import org.jgroups.protocols.MFC;
import org.jgroups.protocols.PING;
import org.jgroups.protocols.UDP;
import org.jgroups.protocols.UFC;
import org.jgroups.protocols.UNICAST2;
import org.jgroups.protocols.VERIFY_SUSPECT;
import org.jgroups.protocols.pbcast.FLUSH;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE_TRANSFER;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;

import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction.ActionType;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatMessage;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatState;

import com.google.protobuf.InvalidProtocolBufferException;


public class Client {
    
    private static final String MANAGEMENT_CHANNEL = "ChatManagement768264";
    
    private JChannel management;
    private Map<String, JChannel> channels = new HashMap<String, JChannel>();
    private Map<String, Set<String>> membership = new HashMap<String, Set<String>>();
    
    private Map<Address, String> users = new HashMap<Address, String>();
    
    private String nick;

    public Client(String nick) {
        try {
            this.nick = nick;
            management = new JChannel();
            ProtocolStack stack = new ProtocolStack();
            management.setProtocolStack(stack);
            buildStack(stack, new UDP());
            management.setReceiver(managementListener);
            management.connect(MANAGEMENT_CHANNEL);
            management.getState(null, 5000);
            System.out.println("Joined the management group");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void close() {
        Set<String> copy = new HashSet<String>(channels.keySet());
        for (String channel: copy) {
            leaveChannel(channel);
        }
        management.close();
    }
    
    
    private Receiver managementListener = new ReceiverAdapter() {

        @Override
        public void getState(OutputStream output) throws Exception {
            ChatState state = buildState();
            state.writeTo(output);
        }
        

        @Override
        public synchronized void receive(Message message) {
            byte[] data = message.getBuffer();
            try {
                ChatAction action = ChatAction.parseFrom(data);
                String channel = action.getChannel();
                String nick = action.getNickname();
                Address addr = message.getSrc();
                if (action.getAction() == ActionType.JOIN) {
                    addEntry(channel, nick, membership);
                    System.out.printf("\r[%s (%s)] joined [%s]\n", nick, 
                            addr, channel);
                    users.put(addr, nick);
                } else {
                    removeEntry(channel, nick, membership);
                    System.out.printf("\r[%s] left [%s]\n", nick, channel);
                    if (countEntries(nick, membership) == 0) {
                        users.remove(addr);
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                System.err.println("Invalid ChatAction message");
                e.printStackTrace(System.err);
            }
        }

        @Override
        public void setState(InputStream input) throws Exception {
            ChatState state = ChatState.parseFrom(input);
            membership = readState(state);
            System.out.println("\r[State synchronized]");
        }

        @Override
        public void viewAccepted(View view) {
            System.out.println("View accepted");
            for (Iterator<Address> it = users.keySet().iterator(); 
                    it.hasNext();) {
                Address addr = it.next();
                if (! view.containsMember(addr)) {
                    it.remove();
                    String nick = users.get(addr);
                    System.out.printf("[%s (%s) crashed]", nick, addr);
                    removeAll(nick, membership);
                }
            }
        }
        
    };
    
    private class ChannelListener extends ReceiverAdapter {

        private String channel;
        
        public ChannelListener(String channel) {
            this.channel = channel;
        }
        
        @Override
        public void receive(Message message) {
            byte[] data = message.getBuffer();
            try {
                ChatMessage msg = ChatMessage.parseFrom(data);
                System.out.printf("\r[%s] %s: %s\n", channel, message.getSrc(), 
                        msg.getMessage());
            } catch (InvalidProtocolBufferException e) {
                System.err.println("\r[Invalid message]");
            }
        }

        @Override
        public void viewAccepted(View view) {
            System.out.println("View accepted");
        }
        
    }
    
    public synchronized void joinChannel(String name) throws Exception {
        if (! membership.containsKey(name)) {
            System.out.println("Creating new channel...");
        }
        JChannel channel = new JChannel(false);
        ProtocolStack stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        buildStack(stack, makeUDP(name));
        channels.put(name, channel);
        channel.setReceiver(new ChannelListener(name));
        channel.connect(name);
        sendJoinMessage(name);
    }
    
    public synchronized void leaveChannel(String name) {
        if (! channels.containsKey(name)) {
            System.err.println("Trying to leave channel you aren't a member of");
            return;
        }
        sendLeaveMessage(name);
        JChannel channel = channels.get(name);
        channel.close();
        channels.remove(name);
        removeEntry(name, nick, membership);
    }
    
    public synchronized void sendMessage(String channel, String content) {
        ChatMessage msg = ChatMessage.newBuilder()
                .setMessage(content)
                .build();
        byte[] data = msg.toByteArray();
        Channel ch = channels.get(channel);
        if (ch == null) {
            System.err.println("No such channel: " + channel);
        }
        try {
            ch.send(new Message(null, data));
        } catch (Exception e) {
            System.err.println("Error while sending message");
            throw new RuntimeException(e);
        }
    }
    
    private void sendJoinMessage(String channel) {
        ChatAction action = buildJoinAction(channel, nick);
        sendChatAction(action);
    }
    
    private void sendLeaveMessage(String channel) {
        ChatAction action = buildLeaveAction(channel, nick);
        sendChatAction(action);
    }
    
    private void sendChatAction(ChatAction action) {
        byte[] data = action.toByteArray();
        try {
            management.send(new Message(null, data));
        } catch (Exception e) {
            System.err.println("Error while sending JOIN message");
            throw new RuntimeException(e);
        }
    }
    
    private Protocol makeUDP(String name) {
        try {
            return new UDP()
                .setValue("mcast_group_addr", InetAddress.getByName(name));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host '" + name + "'");
            throw new RuntimeException(e);
        }
    }
    
    private void buildStack(ProtocolStack stack, Protocol udp) throws Exception {
        stack
        .addProtocol(udp)
        .addProtocol(new PING())
        .addProtocol(new MERGE2())
        .addProtocol(new FD_SOCK())
        .addProtocol(new FD_ALL()
            .setValue("timeout", 12000)
            .setValue("interval", 3000))
        .addProtocol(new VERIFY_SUSPECT())
        .addProtocol(new BARRIER())
        .addProtocol(new NAKACK())
        .addProtocol(new UNICAST2())
        .addProtocol(new STABLE())
        .addProtocol(new GMS())
        .addProtocol(new UFC())
        .addProtocol(new MFC())
        .addProtocol(new FRAG2())
        .addProtocol(new STATE_TRANSFER())
        .addProtocol(new FLUSH());
        stack.init();
    }

    private ChatState buildState() {
        ChatState.Builder builder = ChatState.newBuilder();
        for (Entry<String, Set<String>> channel : membership.entrySet()) {
            for (String user : channel.getValue()) {
                builder.addState(buildJoinAction(channel.getKey(), user));
            }
        }
        return builder.build();
    }
    
    private ChatAction buildJoinAction(String channel, String nick) {
        return buildAction(channel, nick, ActionType.JOIN);
    }
    
    private ChatAction buildLeaveAction(String channel, String nick) {
        return buildAction(channel, nick, ActionType.LEAVE);
    }
    
    private ChatAction buildAction(String channel, String nick, 
            ActionType action) {
        return ChatAction.newBuilder()
                .setAction(action)
                .setChannel(channel)
                .setNickname(nick)
                .build();
    }
    
    private Map<String, Set<String>> readState(ChatState state) {
        Map<String, Set<String>> users = new HashMap<String, Set<String>>();
        for (ChatAction action: state.getStateList()) {
            String channel = action.getChannel();
            String nick = action.getNickname();
            addEntry(channel, nick, users);
        }
        return users;
    }
    
    private static void addEntry(String channel, String nick, 
            Map<String, Set<String>> users) {
        Set<String> channelSet = users.get(channel);
        if (channelSet == null) {
            channelSet = new HashSet<String>();
            users.put(channel, channelSet);
        }
        channelSet.add(nick);
    }
    
    private static boolean removeEntry(String channel, String nick, 
            Map<String, Set<String>> users) {
        Set<String> channelSet = users.get(channel);
        if (channelSet == null) {
            return false;
        } else {
            boolean removed = channelSet.remove(nick);
            if (channelSet.isEmpty()) {
                users.remove(channel);
            }
            return removed;
        }
    }
    
    private static void removeAll(String nick, 
            Map<String, Set<String>> users) {
        for (Set<String> set: users.values()) {
            set.remove(nick);
        }
    }
    
    private static int countEntries(String nick, 
            Map<String, Set<String>> users) {
        int count = 0;
        for (Set<String> set: users.values()) {
            if (set.contains(nick)) {
                ++ count;
            }
        }
        return count;
    }
    
    public Map<String, Set<String>> getMembership() {
        return membership;
    }
    
    public Set<String> getChannels() {
        return channels.keySet();
    }
    
    public String getNick() {
        return nick;
    }

}
