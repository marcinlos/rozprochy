package rozprochy.iiice.test.server;

import Ice.Current;
import rozprochy.iiice.test._PrinterDisp;

public class PrinterI extends _PrinterDisp {

    public PrinterI() {
        System.out.println("Created");
    }

    @Override
    public void print(String s, Current current) {
        System.out.println("Printing: " + s);
    }

}
