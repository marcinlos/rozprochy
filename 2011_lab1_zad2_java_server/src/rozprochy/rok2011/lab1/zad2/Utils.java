package rozprochy.rok2011.lab1.zad2;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Class containing utility methods
 */
public abstract class Utils {

    /**
     * Formats socket's remote endpoint's address as ip:port
     */
    public static String formatAddress(Socket socket) {
        InetAddress remote = socket.getInetAddress();
        String ip = remote.getHostAddress();
        int port = socket.getPort();
        return String.format("%s:%d", ip, port);
    }

}
