package rozprochy.lab4.chat.server;

import static rozprochy.lab4.util.IOExceptionWrapper.wrapIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.SessionListener;
import rozprochy.lab4.util.IOExceptionWrapper;
import Chat.CannotCreateRoom;
import Chat.InvalidRoomName;
import Chat.RoomAlreadyExists;
import Ice.Current;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.ServantLocator;
import Ice.UserException;

public class RoomManager implements ServantLocator {
    
    // All the rooms
    private Set<String> rooms = new HashSet<String>();
    
    // Rooms currently active (servants exist)
    private Map<String, RoomImpl> activeRooms = new HashMap<String, RoomImpl>();
    
    // Lock guarding active rooms map
    private Lock activeRoomsLock = new ReentrantLock();
    
    // Map with activeRooms each player is subscribed to
    private Map<String, Set<String>> users = new HashMap<String, Set<String>>();
    
    // Lock guarding user->rooms map
    private Lock usersLock = new ReentrantLock();
    
    private Map<String, String> config;
    
    private BiSessionManager sessions;

    /** Root directory of a room database */
    private File dbRoot;
    private String listFile = "list";
    
    private static final String PREFIX = "[Chat] ";
    
    
    public RoomManager(Map<String, String> config, BiSessionManager sessions) {
        this.config = config;
        this.sessions = sessions;
        System.out.println(PREFIX + "Initiating room manager");
        loadConfig();
        try {
            loadRoomList();
        } catch (IOException e) {
            System.err.println(PREFIX + "Error while reading room database");
            throw new RuntimeException(e);
        }
        sessions.addSessionListener(new SessionListener<BiSession>() {
            @Override
            public void sessionRemoved(BiSession session, RemovalReason reason) {
                String user  = session.getUser();
                Set<String> userRooms;
                System.out.printf("%sUser %s is gone, removing from rooms " + 
                        "(%s)\n", PREFIX, user, reason);
                usersLock.lock();
                try {
                    if (users.containsKey(user)) {
                        userRooms = users.get(user);
                        activeRoomsLock.lock();
                        try {
                            for (String roomName: userRooms) {
                                RoomImpl room = activeRooms.get(roomName);
                                if (room == null) {
                                    room = activateRoom(roomName);
                                }
                                room.userSessionTerminated(session, reason);
                            }
                        } finally {
                            activeRoomsLock.unlock();
                        }
                    }
                } finally {
                    usersLock.unlock();
                }
            }
        });
        if (rooms.isEmpty()) {
            System.out.println("Adding test room...");
            try {
                createRoom("Gauss");
            } catch (CannotCreateRoom e) {
                e.printStackTrace();
            }
        }
    }
    
    /*
     * Loads properties from configuration map passed in the constructor
     */
    private void loadConfig() {
        System.out.println(PREFIX + "Loading room manager configuration");
        String dir = config.get("ChatApp.Rooms.Root");
        if (dir == null) {
            System.err.println(PREFIX + "Warning: room storage directory " +
                    "not specified, using default");
            dir = ".chatdb/rooms";
        }
        dbRoot = new File(dir);
    }
    
    /*
     * Loads room list into memory (names only) form well-known file if it
     * exists, or creates this file if it does not.
     */
    private void loadRoomList() throws IOException {
        System.out.println(PREFIX + "Initialized storage in " + dbRoot);
        File list = new File(dbRoot, listFile);
        dbRoot.mkdirs();
        if (list.createNewFile()) {
            System.out.println(PREFIX + list + " did not exist, was created");
        } else {
            readRoomList(list);
            System.out.printf("%s   Found %d rooms(s):\n", PREFIX, rooms.size());
            for (String room: rooms) {
                System.out.printf("%s     - %s\n", PREFIX, room);
            }
        }
    }
    
    /*
     * Actually reads the file containing room names, and fills room set.
     */
    private void readRoomList(File list) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(list);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (! isValidRoomName(line)) {
                    throw new RuntimeException("Corrupted database");
                }
                rooms.add(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println(PREFIX + "List deleted before read");
            throw wrapIO(e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
    
    public void createRoom(String name) throws CannotCreateRoom {
        synchronized (rooms) {
            if (rooms.contains(name)) {
                throw new RoomAlreadyExists();
            } else if (! isValidRoomName(name)) {
                throw new InvalidRoomName();
            } else {
                rooms.add(name);
                addRoomToList(name);
                try {
                    new RoomDatabase(dbRoot, name);
                } catch (IOExceptionWrapper e) {
                    throw new CannotCreateRoom(e);
                }
            }
        }
    }
    
    public List<String> getRoomList() {
        synchronized (rooms) {
            return new ArrayList<String>(rooms);
        }
    }
    
    private RoomImpl activateRoom(String name) {
        activeRoomsLock.lock();
        try {
            System.out.println(PREFIX + "Activating room " + name);
            RoomDatabase db = new RoomDatabase(dbRoot, name);
            RoomImpl room = new RoomImpl(name, this, db, sessions);
            activeRooms.put(name, room);
            return room;
        } finally {
            activeRoomsLock.unlock();
        }
    }
    
    private void addRoomToList(String name) {
        File list = new File(dbRoot, listFile);
        FileWriter out = null;
        try {
            out = new FileWriter(list, true);
            out.write(name + "\n");
        } catch (IOException e) {
            System.err.println(PREFIX + "Error while adding room to room list");
            throw wrapIO(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    System.err.println(PREFIX + "Error closing room list");
                }
            }
        }
    }

    /*
     * Checks whether a name is a valid name for a room. It is used to check
     * database integrity, and to validate names of newly created rooms.
     */
    private boolean isValidRoomName(String name) {
        // TODO Maybe alphanumeric only?
        return true;
    }
    
    
    public void addUserRoom(String user, String room) {
        usersLock.lock();
        try {
            Set<String> set = users.get(user);
            if (set == null) {
                set = new HashSet<String>();
            }
            set.add(room);
            users.put(user, set);
        } finally {
            usersLock.unlock();
        }
    }
    
    
    public void removeUserRoom(String user, String room) {
        usersLock.lock();
        try {
            Set<String> set = users.get(user);
            if (set == null) {
                set = new HashSet<String>();
            }
            set.remove(room);
            users.put(user, set);
        } finally {
            usersLock.unlock();
        }
    }

    @Override
    public Object locate(Current curr, LocalObjectHolder cookie)
            throws UserException {
        activeRoomsLock.lock();
        try {
            String name = curr.id.name;
            System.out.println(PREFIX + "Looking for room " + name + "...");
            if (activeRooms.containsKey(name)) {
                return activeRooms.get(name);
            } else {
                synchronized (rooms) {
                    if (rooms.contains(curr.id.name)) {
                        RoomImpl room = activateRoom(name);
                        return room;
                    } else {
                        return null;
                    }
                }
            }
        } finally {
            activeRoomsLock.unlock();
        }
    }

    @Override
    public void finished(Current curr, Object servant, java.lang.Object cookie)
            throws UserException {
        // empty
    }

    @Override
    public void deactivate(String category) {
        // empty
    }

}
