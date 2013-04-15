#include <iostream>
#include <Ice/Ice.h>
#include "generated/Testbed.h"
#include "Client.hpp"

using MiddlewareTestbed::AFactoryPtr;
using MiddlewareTestbed::AFactoryPrx;

using rozprochy::lab3::Client;


int main(int argc, char* argv[])
{
    std::istringstream ss("123");
    int c;
    ss >> c;
    if (! ss) { std::cout << "HAFSHDFSDHFSDFSDFSDFSDF" << std::endl; }
    int status = 0;
    Ice::CommunicatorPtr ice;
    try
    {
        ice = Ice::initialize(argc, argv);
        std::cout << "Attempting to get a reference..." << std::endl;
        Ice::ObjectPrx object = ice->stringToProxy("mlosFactory:default -p 6666");
        std::cout << "Casting..." << std::endl;
        AFactoryPrx factory = AFactoryPrx::checkedCast(object);
        if (factory)
        {
            Client client(ice, factory);
            client.run();
        }
        else
        {
            std::cerr << "Failed to obtain factory remote reference";
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

