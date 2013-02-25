#include <iostream>
#include <string>
#include <Ice/Ice.h>
#include "generated/Printer.h"

class PrinterI: public rozprochy::iiice::test::Printer
{
    virtual void print(const std::string& text, const Ice::Current&)
    {
        std::cout << text << std::endl;
    }
};

int main(int argc, char* argv[])
{
    int status = 0;
    Ice::CommunicatorPtr ic;
    try
    {
        ic = Ice::initialize(argc, argv);
        Ice::ObjectAdapterPtr adapter = ic->createObjectAdapterWithEndpoints(
                "SimplePrinterAdapter", "default -p 10000");
        Ice::ObjectPtr object = new PrinterI;
        adapter->add(object, ic->stringToIdentity("SimplePrinter"));
        adapter->activate();
        ic->waitForShutdown();
    }
    catch (const Ice::Exception& e)
    {
        std::cerr << e << std::endl;
        status = 1;
    }
    if (ic)
    {
        try
        {
            ic->destroy();
        }
        catch (const Ice::Exception& e)
        {
            std::cerr << e << std::endl;
            status = 1;
        }
    }
    return status;
}

