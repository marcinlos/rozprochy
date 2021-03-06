package rozprochy.rok2011.lab1.zad2;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Class handling all the interaction with the client
 */
class ClientHandler implements Runnable {
    
    private final Logger logger = 
            Logger.getLogger(ClientHandler.class.getName());
    
    /** Size of a buffer for reading/writing file */
    private static final int BUFFER_SIZE = 2048;

    private Socket client;
    private File directory;
    private File localFile;
    
    /** Amount of bytes that remain to be read */
    private int remaining;
    
    /** Input (socket) stream */
    private DataInputStream input;
    
    /** Output (file) stream */
    private OutputStream output;
    
    /** Textual representation of client's endpoint */
    private String endpoint;
    
    
    public ClientHandler(Socket client, File directory) {
        this.client = client;
        this.directory = directory;
        endpoint = Utils.formatAddress(client);
    }
    
    
    @Override
    public void run() {
        try {
            doWork();
        } catch (EOFException e) {
            logger.severe("At " + endpoint + ": Unexpected end of connection");
        } catch (IOException e) {
            logger.severe("At " + endpoint + ": Error: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            try { 
                client.close();
            }
            catch (IOException e) {
                logger.severe("At " + endpoint + ": Error while closing: " + 
                        e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }
    
    
    private void deleteFile() {
        if (localFile != null) {
            if (!localFile.delete()) {
                logger.severe("At " + endpoint + ": Cannot delete local " + 
                        "file after failure");
            }
        }
    }
    
    
    /**
     * Method actually handling the connection details - extracted from
     * {@link #run()} to separate it from exception handling details
     */
    private void doWork() throws IOException {
        output = null;
        try {
            input = new DataInputStream(client.getInputStream());
            output = makeFile();
            remaining = readFileSize();
            receiveFile();
        } catch (IOException e) {
            // cleanup & rethrow
            deleteFile();
            throw e;
        } catch ( RuntimeException e) {
            // cleanup & rethrow
            deleteFile();
            throw e;
        } finally {
            try { output.close(); }
            catch (IOException e) { /* fix it, maybe? */ }
        }
    }
    
    /**
     * Reads a file size from the socket
     */
    private int readFileSize() throws IOException {
        return input.readInt();
    }

    /**
     * Reads the whole file and saves the content locally
     */
    private void receiveFile() throws IOException {
        
        byte[] buffer = new byte[BUFFER_SIZE];
        
        while (remaining > 0) {
            int n = input.read(buffer);
            if (n > 0) {
                remaining -= n;
                output.write(buffer, 0, n);
            } else {
                // Stream is closed before we received the whole file
                logger.severe("EOF received, but " + remaining + "B " +
                        "more were expected");
                break;
            }
        }
    }
    

    /**
     * Opens an output file, with name as returned by 
     * {@link #createFileName()}.
     * 
     * @return output file stream 
     */
    private OutputStream makeFile() throws IOException {
        localFile = createFileName();
        return new BufferedOutputStream(
                new FileOutputStream(localFile));
    }
    
    /**
     * Creates a reasonably likely unique name for a file (combination
     * of an address and current time)
     */
    private File createFileName() {
        InetAddress remote = client.getInetAddress();
        String ip = remote.getHostAddress().replace('.', '_');
        int port = client.getPort();
        long time = System.nanoTime();
        String name = String.format("%s_%d_%x", ip, port, time);
        return new File(directory, name);
    }
    
}