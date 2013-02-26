
import Ice, sys, traceback
from rozprochy.iiice.test import Printer
try: from Printer_ice import Printer
except: pass

class PrinterI(Printer):
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
        print 'Waiting for connections...'
        ic.waitForShutdown()
    except KeyboardInterrupt:
        pass
    except BaseException:
        traceback.print_exc()
        status = 1
    finally:
        if ic:
            try:
                ic.destroy()
            except:
                traceback.print_exc()
                status = 1
        print 'Shutting down'
    sys.exit(status)

