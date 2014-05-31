
import sys
import Ice
import Bank
import readline

class Client(Ice.Application):
    def run(self, args):
        self.ice = self.communicator()
        obj = self.ice.stringToProxy('Bank/Manager:ssl -p 6667')
        self.bank = Bank.BankManagerPrx.checkedCast(obj)
        self.inputLoop()

    def inputLoop(self):
        while True:
            try:
                line = raw_input()
                self.interpret(line)
            except EOFError:
                break

    def interpret(self, line):
        print 'Input: ', line.rstrip()

if __name__ == '__main__':
    app = Client()
    status = app.main(sys.argv)
    sys.exit(status)
