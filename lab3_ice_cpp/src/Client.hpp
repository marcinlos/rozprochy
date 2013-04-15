
#ifndef CLIENT_HPP_
#define CLIENT_HPP_

#include <map>
#include <string>
#include <sstream>
#include <Ice/Communicator.h>
#include "generated/Testbed.h"


namespace rozprochy {
namespace lab3 {

using MiddlewareTestbed::ItemPrx;
using MiddlewareTestbed::AFactoryPrx;

class Client
{
private:
    Ice::CommunicatorPtr ice;
    AFactoryPrx factory;
    std::map<std::string, ItemPrx> owned;

    typedef void (Client::*command)(std::istream&);
    typedef std::map<std::string, command>::const_iterator map_iter;
    std::map<std::string, command> commands;

    void interpret(const std::string& line);

    void create(std::istream& in);
    void take(std::istream& in);
    void release(std::istream& in);

    void age(std::istream& in);

    void call(std::istream& in);
    void dynamic_invoke(Ice::ObjectPrx obj, std::istream& in);
public:

    Client(Ice::CommunicatorPtr ice, AFactoryPrx factory)
    : ice(ice), factory(factory)
    {
        commands["create"]  = &Client::create;
        commands["take"]    = &Client::take;
        commands["release"] = &Client::release;
        commands["age"]     = &Client::age;
        commands["call"]    = &Client::call;
    }

    void run();
};



} // lab3
} // rozprochy

#endif /* CLIENT_HPP_ */
