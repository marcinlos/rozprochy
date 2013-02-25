
import Ice, sys, traceback
import rozprochy.iiice.test

if __name__ == '__main__':
    status = 0
    ic = None
    try:
        ic = Ice.initialize(sys.argv)
        base = ic.stringToProxy('SimplePrinter:default -p 10000')
        printer = rozprochy.iiice.test.PrinterPrx.checkedCast(base)
        if not printer:
            raise RuntimeError('Cannot obtain printer proxy')
        printer._print('Hello world from Python!')
    except:
        traceback.print_exc()
        status = 1
    sys.exit(status)
        