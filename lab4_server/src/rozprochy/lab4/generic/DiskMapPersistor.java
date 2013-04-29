package rozprochy.lab4.generic;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rozprochy.lab4.util.DiskMap;
import rozprochy.lab4.util.IOExceptionWrapper;

public class DiskMapPersistor<T extends AccountData> implements
        AccountPersistor<T> {
    
    private DiskMap<T> map;
    
    private Lock dataLock = new ReentrantLock();

    public DiskMapPersistor(File baseDir) throws IOException {
        try {
            map = new DiskMap<T>(baseDir);
        } catch (IOExceptionWrapper e) {
            throw e.wrapped();
        }
    }

    @Override
    public T load(String login) throws IOException {
        try {
            return map.get(login);
        } catch (IOExceptionWrapper e) {
            throw e.wrapped();
        }
    }

    @Override
    public void save(T account) throws IOException {
        try {
            map.put(account.getLogin(), account);
        } catch (IOExceptionWrapper e) {
            throw e.wrapped();
        }
    }

    @Override
    public boolean exists(String login) throws IOException {
        try {
            return map.containsKey(login);
        } catch (IOExceptionWrapper e) {
            throw e.wrapped();
        }
    }

    @Override
    public T lock(String login) throws IOException {
        lock();
        try {
            T acc = load(login);
            if (acc == null) {
                unlock();
            }
            return acc;
        } catch (IOException e) {
            unlock();
            return null;
        }
    }

    @Override
    public void commit(T account) throws IOException {
        String login = account.getLogin();
        if (exists(login)) {
            save(account);
        }
        unlock(login);
    }

    @Override
    public void unlock(String login) {
        unlock();
    }

    @Override
    public int size() throws IOException {
        try {
            return map.size();
        } catch (IOExceptionWrapper e) {
            throw e.wrapped();
        }
    }

    @Override
    public void lock() {
        dataLock.lock();
    }

    @Override
    public void unlock() {
        dataLock.unlock();
    }
    
}
