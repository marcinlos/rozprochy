package rozprochy.lab4.chat.server;

import static rozprochy.lab4.util.IOExceptionWrapper.wrapIO;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
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

    private String name;
    private File base;

    private static final String INDEX = "idx";
    private static final String MSGS = "db";
    
    private File index;
    private File msgs;
    
    private DiskMap<Long> users;
    
    private static final String PREFIX = "[Chat] ";
    
    public RoomDatabase(File root, String name) {
        this.name = name;
        base = new File(root, name);
        index = new File(base, INDEX);
        msgs = new File(base, MSGS);
        try {
            users = new DiskMap<Long>(base);
            if (index.createNewFile()) {
                msgs.createNewFile();
                System.out.println(PREFIX + " (Room " + name + ") Creating new " +
                        "room database");
            }
        } catch (IOException e) {
            System.err.println(PREFIX + "Error while initialising database");
            throw wrapIO(e);
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
            throw wrapIO(e);
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
    private long offsetOf(long id) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(index, "r");
            if (8 * id >= file.length()) {
                return -1;
            }
            file.seek(id * 8);
            return file.readLong();
        } catch (IOException e) {
            throw wrapIO(e);
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                System.err.println(PREFIX + "(Room " + name + ") Error " +
                        "while reading messages");
            }
        }
    }
    
    public synchronized Message readMessage(long id) {
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
            } catch (IOException e) {
                throw wrapIO(e);
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
    
    public synchronized Message[] readSince(long id) {
        List<Message> messages = new ArrayList<Message>();
        while (true) {
            Message msg = readMessage(id ++);
            if (msg != null) {
                messages.add(msg);
            } else {
                break;
            }
        }
        return messages.toArray(new Message[0]);
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
                System.err.println(PREFIX + "(Room " + name + ") Error " + 
                        "while updating index");
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
    
    public boolean userExists(String user) {
        synchronized (users) {
            return users.containsKey(user);
        }
    }
    
    public Message[] getPending(String user) {
        synchronized (users) {
            if (users.containsKey(user)) {
                long id = users.get(user);
                return readSince(id + 1);
            } else {
                return null;
            }
        }
    }

}
