package rozprochy.lab4.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class DiskMap<T extends Serializable> extends AbstractMap<String, T> {

    private static final String KEY_FILE = "keys.txt";

    private File baseDir;
    private File keysFile;

    public DiskMap(File directory) throws IOException {
        this.baseDir = directory;
        this.keysFile = new File(directory, KEY_FILE);
        baseDir.mkdir();
        keysFile.createNewFile();
    }

    @Override
    public T put(final String key, final T value) {
        T prev = get(key);
        try {
            writeToFile(key, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return prev;
    }

    @Override
    public T get(Object key) {
        if (key instanceof String) {
            String k = (String) key;
            try {
                return readFromFile(k);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "resource" })
    private T readFromFile(String name) throws IOException {
        File file = new File(baseDir, name);
        ObjectInput input = null;
        try {
            FileInputStream fileStream = new FileInputStream(file);
            input = new ObjectInputStream(fileStream);
            return (T) input.readObject();
        } catch (FileNotFoundException e) {
            return null;
        } catch (ClassNotFoundException e) {
            // Should never happen unless the data is corrupted
            throw new RuntimeException(e);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @SuppressWarnings("resource")
    private void writeToFile(String key, T value) throws IOException {
        if (!containsKey(key)) {
            appendKey(key);
        }
        File file = new File(baseDir, key);
        ObjectOutput output = null;
        try {
            file.createNewFile();
            FileOutputStream fileStream = new FileOutputStream(file);
            output = new ObjectOutputStream(fileStream);
            output.writeObject(value);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void appendKey(String encoded) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(keysFile, true));
            writer.println(encoded);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            writer.close();
        }
    }

    @Override
    public Set<Map.Entry<String, T>> entrySet() {
        return new AbstractDiskMapSet<Map.Entry<String, T>>() {
            @Override
            protected Map.Entry<String, T> transform(String name)
                    throws IOException {
                final T value = readFromFile(name);
                return new Entry<T>(value, name);
            }
        };
    }

    @Override
    public Set<String> keySet() {
        return new AbstractDiskMapSet<String>() {
            @Override
            protected String transform(String str) {
                return str;
            }
        };
    }

    private static class Entry<V> implements Map.Entry<String, V> {
        private final V value;
        private final String name;

        private Entry(V value, String name) {
            this.value = value;
            this.name = name;
        }

        @Override
        public String getKey() {
            return name;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

    private abstract class AbstractDiskMapSet<V> extends AbstractSet<V> {

        private class MapIterator implements Iterator<V> {

            final Scanner scanner;

            public MapIterator() {
                try {
                    this.scanner = new Scanner(keysFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean hasNext() {
                return scanner.hasNextLine();
            }

            @Override
            public V next() {
                String line = scanner.nextLine();
                try {
                    return transform(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        @Override
        public Iterator<V> iterator() {
            return new MapIterator();
        }

        @Override
        public int size() {
            Iterator<?> iter = new MapIterator() {
                @Override
                public V next() {
                    scanner.nextLine();
                    return null;
                }
            };
            int count = 0;
            for (; iter.hasNext(); iter.next()) {
                ++count;
            }
            return count;
        }

        protected abstract V transform(String encoded) throws IOException;
    };

}
