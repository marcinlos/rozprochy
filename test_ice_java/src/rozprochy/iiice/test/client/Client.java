package rozprochy.iiice.test.client;

import rozprochy.iiice.test.PrinterPrx;
import rozprochy.iiice.test.PrinterPrxHelper;

public class Client {

    public static void main(String[] args) {
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            String address = "SimplePrinter:default -p 10000";
            Ice.ObjectPrx obj = ic.stringToProxy(address);
            PrinterPrx printer = PrinterPrxHelper.checkedCast(obj);
            if (printer != null) {
                printer.print("Hello, world");
            }
        } catch (Ice.LocalException e) { 
            e.printStackTrace(System.err);
        } catch (Exception e) { 
            e.printStackTrace(System.err);
        } finally {
            if (ic != null) {
                try { ic.destroy(); }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

}
