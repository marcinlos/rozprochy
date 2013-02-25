#include <iostream>
#include <Ice/Ice.h>
#include "generated/Printer.h"

using rozprochy::iiice::test::PrinterPtr;
using rozprochy::iiice::test::PrinterPrx;


int main(int argc, char* argv[])
{
    int status = 0;
    Ice::CommunicatorPtr ic;
    try
    {
        ic = Ice::initialize(argc, argv);
        Ice::ObjectPrx object = ic->stringToProxy("SimplePrinter:default -p 10000");
        PrinterPrx printer = PrinterPrx::checkedCast(object);
        if (printer)
        {
            printer->print("Hello world from C++!");
        }
        else
        {
            std::cerr << "Failed to obtain Printer remote reference";
            status = 1;
        }
    }
    catch (const Ice::Exception& e)
    {
        std::cerr << e << std::endl;
        status = 1;
    }
    return status;
}


