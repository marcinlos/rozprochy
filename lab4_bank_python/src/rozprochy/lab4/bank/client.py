
import sys
import Ice
import Bank
# To satisfy PyDev's static analyzer
if 'SystemManagerPrx' not in Bank.__dict__:
    from Bank import Bank_ice as Bank


class Client(Ice.Application):
    def run(self, args):
        self.ice = self.communicator()
        obj = self.ice.stringToProxy('Bank/Manager:ssl -p 6667')
        self.bank = Bank.SystemManagerPrx.checkedCast(obj)
        pass

if __name__ == '__main__':
    app = Client()
    status = app.main(sys.argv)
    sys.exit(status)
