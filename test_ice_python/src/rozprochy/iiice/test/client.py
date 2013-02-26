
import Ice, sys, traceback
from rozprochy.iiice.test import PrinterPrx
try: from Printer_ice import PrinterPrx
except: pass


if __name__ == '__main__':
    status = 0
    ic = None
    try:
        ic = Ice.initialize(sys.argv)
        base = ic.stringToProxy('SimplePrinter:default -p 10000')
        printer = PrinterPrx.checkedCast(base)
        if not printer:
            raise RuntimeError('Cannot obtain printer proxy')
        printer._print('Hello world from Python!')
    except:
        traceback.print_exc()
        status = 1
    finally:
        if ic:
            try:
                ic.destroy()
            except:
                traceback.print_exc()
                status = 1
    sys.exit(status)
        