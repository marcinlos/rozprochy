package rozprochy.lab4.chat.server;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import rozprochy.lab4.util.DiskMap;
import Chat.Message;

/*
 * ACHTUNG
 * Nasty, evil, sloppy code inside
 */

public class RoomDatabase {

    private File root;
    private String name;
    private File base;

    private static final String INDEX = "idx";
    private static final String MSGS = "db";
    
    private File index;
    private File msgs;
    
    private DiskMap<Long> users;
    
    private static final String PREFIX = "[Chat] ";
    
    public RoomDatabase(File root, String name) throws IOException {
        this.root = root;
        this.name = name;
        base = new File(root, name);
        index = new File(base, INDEX);
        msgs = new File(base, MSGS);
        users = new DiskMap<Long>(base);
        if (index.createNewFile()) {
            msgs.createNewFile();
            System.out.println(PREFIX + " (Room " + name + ") Creating new " +
                    "room database");
        }
    }
    
    public synchronized void addMessage(Message messsage) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(msgs, "rw");
            long pos = file.length();
            file.seek(pos);
            long id = addToIndex(pos);
            messsage.id = id;
            WritableByteChannel ch = file.getChannel();
            ObjectOutput output = new ObjectOutputStream(
                    Channels.newOutputStream(ch));
            output.writeObject(messsage);
            output.close();
        } catch (IOException e) {
            System.err.println(PREFIX + "Error while saving message");
            throw new RuntimeException(e);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    System.err.println(PREFIX + "(Room " + name + ") " +
                            "Error while closing message list");
                }
            }
        }
    }
    
    /*
     * Returns -1 if id is out of range
     */
    private long offsetOf(long id) throws IOException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(index, "r");
            if (8 * id >= file.length()) {
                return -1;
            }
            file.seek(id * 8);
            return file.readLong();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                System.err.println(PREFIX + "(Room " + name + 
                        ") Error while reading messages");
            }
        }
    }
    
    public synchronized Message readMessage(long id) throws IOException {
        long pos = offsetOf(id);
        if (pos == -1) {
            return null;
        } else {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(msgs, "r");
                file.seek(pos);
                ReadableByteChannel channel = file.getChannel();
                ObjectInputStream in = new ObjectInputStream(
                        Channels.newInputStream(channel));
                try {
                    Object o = in.readObject();
                    return (Message) o;
                } catch (ClassNotFoundException e) {
                    System.err.println(PREFIX + "(Room " + name + ") " + 
                            "Database corrupted");
                    throw new RuntimeException(e);
                }
            } finally {
                try {
                    file.close();
                } catch (IOException e) {
                    System.err.println(PREFIX + "(Room " + name + ") Error " + 
                            "while reading index file");
                }
            }
        }
    }
    
    public synchronized List<Message> readSince(long id) throws IOException {
        List<Message> messages = new ArrayList<Message>();
        while (true) {
            Message msg = readMessage(id ++);
            if (msg != null) {
                messages.add(msg);
            } else {
                break;
            }
        }
        return messages;
    }
    
    private long addToIndex(long pos) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(index, "rw");
            long id = file.length() / 8;
            file.seek(file.length());
            file.writeLong(pos);
            return id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                System.err.println(PREFIX + "(Room " + name + 
                        ") Error while updating index");
            }
        }
    }
    
    public void updateUser(String user, long id) {
        synchronized (users) {
            users.put(user, id);
        }
    }
    
    public void removeUser(String user) {
        synchronized (users) {
            users.remove(user);
        }
    }

}
