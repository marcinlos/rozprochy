
import Ice, sys, traceback
import rozprochy.iiice.test

class PrinterI(rozprochy.iiice.test.Printer):
    def _print(self, s, current=None):
        print s

if __name__ == '__main__':
    status = 0
    ic = None
    try:
        ic = Ice.initialize(sys.argv)
        adapter = ic.createObjectAdapterWithEndpoints('SimplePrinterAdapter', 
            'default -p 10000')
        servant = PrinterI()
        adapter.add(servant, ic.stringToIdentity('SimplePrinter'))
        adapter.activate()
        ic.waitForShutdown()
    except:
        traceback.print_exc()
        status = 1
        
    if ic:
        try:
            ic.destroy()
        except:
            traceback.print_exc()
            status = 1
            
    sys.exit(status)

