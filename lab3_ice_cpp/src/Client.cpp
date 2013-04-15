#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <Ice/Initialize.h>
#include "Client.hpp"
#include "DIIParser.hpp"


namespace rozprochy {
namespace lab3 {

using namespace MiddlewareTestbed;


void Client::run()
{
    std::string line;
    while (std::getline(std::cin, line))
    {
        interpret(line);
    }
}


void Client::interpret(const std::string& line)
{
    std::istringstream ss(line);
    std::string cmd;
    ss >> cmd;
    map_iter it = commands.find(cmd);
    if (it != commands.end())
    {
        try { (this->*it->second)(ss); }
        catch (std::exception& e)
        {
            std::cout << "Error: " << e.what() << std::endl;
        }
    }
    else
    {
        std::cout << "Unknown command `" << cmd << std::endl;
    }
}


void Client::create(std::istream& in)
{
    try
    {
        std::string name, type;
        in >> type >> name;
        ItemPrx item = factory->createItem(name, type);
        owned.insert(std::make_pair(name, item));
    }
    catch (const ItemAlreadyExists& e)
    {
        std::cout << "Error: item already exists" << std::endl;
    }
}


void Client::take(std::istream& in)
{
    try
    {
        std::string name;
        in >> name;
        ItemPrx item = factory->takeItem(name);
        owned.insert(std::make_pair(name, item));
    }
    catch (const ItemNotExists& e)
    {
        std::cout << "Error: item does not exist" << std::endl;
    }
    catch (const ItemBusy& e)
    {
        std::cout << "Error: item is busy" << std::endl;
    }
}


void Client::release(std::istream& in)
{
    try
    {
        std::string name;
        in >> name;
        factory->releaseItem(name);
        owned.erase(name);
    }
    catch (const ItemNotExists& e)
    {
        std::cout << "Error: item does not exist" << std::endl;
    }
}


void Client::age(std::istream& in)
{
    std::string name;
    in >> name;
    std::map<std::string, ItemPrx>::iterator it = owned.find(name);
    if (it != owned.end())
    {
        ItemPrx item  = it->second;
        Ice::Long a = item->getItemAge();
        std::cout << a << "s" << std::endl;
    }
    else
    {
        std::cout << "Error: item `" << name << "' does not exits "
                  << "or is not acquired" << std::endl;
    }
}


void Client::call(std::istream& in)
{
    try
    {
        std::string name;
        in >> name;
        std::map<std::string, ItemPrx>::iterator it = owned.find(name);
        if (it != owned.end())
        {
            ItemPrx item  = it->second;
            dynamic_invoke(item, in);
        }
        else
        {
            std::cout << "Error: item `" << name << "' does not exits "
                    << "or is not acquired" << std::endl;
        }
    }
    catch (const DIIUnknownType& e)
    {
        std::cout << "Error: unknown type `" << e.type << "'" << std::endl;
    }
    catch (const DIIInvalidValue& e)
    {
        std::cout << "Error: invalid value `" << e.value << "'" << std::endl;
    }
    catch (const DIIUnknownMode& e)
    {
        std::cout << "Error: unknown mod `" << e.mode << "'" << std::endl;
    }
    catch (const DIIIncomplete& e)
    {
        std::cout << "Incomplete command" << std::endl;
    }
    catch (const Ice::LocalException& e)
    {
        std::cout << "Error: " << e << std::endl;
    }
    catch (const std::exception& e)
    {
        std::cout << e.what() << std::endl;
    }
}

void Client::dynamic_invoke(Ice::ObjectPrx obj, std::istream& in)
{
    DIIParser dii(in, ice);
    if (dii(obj))
    {
        std::cout << "cool" << std::endl;
    }
}


} // lab3
} // rozprochy
