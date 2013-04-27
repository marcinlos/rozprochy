package rozprochy.lab4.signer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import Ice.Properties;
import SR.CertSignerPrx;
import SR.CertSignerPrxHelper;


public class Main {
    
    public static void main(String[] args) {
        Ice.Communicator ice = null;
        try {
            if (args.length < 2) {
                System.err.println("Usage: signer --Ice.Config=<conf> <csr file>");
                System.exit(1);
            }
            File csrFile = new File(args[1]);
            File crtFile = toCrt(csrFile);
            byte[] csr = readFile(csrFile);

            ice = Ice.Util.initialize(args);
            Properties props = ice.getProperties();
            
            Ice.ObjectPrx obj = ice.propertyToProxy("Signer.Proxy");
            CertSignerPrx signer = CertSignerPrxHelper.checkedCast(obj);
            String surname = props.getProperty("User.Surname");
            String name = props.getProperty("User.Name");
            
            byte[] crt = signer.signCSR(name, surname, csr);
            writeFile(crtFile, crt);
        } catch (InvalidNameException e) {
            System.err.println(e);
        } catch (Exception e) { 
            handleException(e);
        } finally {
            destroyIce(ice);
        }
    }
    
    private static void destroyIce(Ice.Communicator ice) {
        if (ice != null) {
            try { ice.destroy(); }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private static File toCrt(File csr) throws InvalidNameException {
        String path = csr.getPath();
        if (! path.endsWith(".csr")) {
            throw new InvalidNameException(csr.getName());
        }
        String crtPath = path.substring(0, path.length() - 4) + ".crt";
        return new File(crtPath);
    }
    
    private static byte[] readFile(File file) throws IOException {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = input.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } finally {
            safeClose(input);
        }
    }
    
    private static void safeClose(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (IOException e) {
                System.err.println("Error while closing");
                handleException(e);
            }
        }
    }
    
    private static void writeFile(File file, byte[] data) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(data);
        } finally {
            safeClose(output);
        }
    }
    
    private static void handleException(Throwable e) {
        System.err.println("Shutting down, see crash.log for details");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("crash.log");
            writer.println("Fatal error: " + e.getMessage());
            e.printStackTrace(writer);
        } catch (FileNotFoundException e1) {
            System.err.println("Cannot create log file");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}
