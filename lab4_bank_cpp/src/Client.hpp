/*
 * Client.hpp
 *
 *  Created on: Apr 28, 2013
 *      Author: los
 */

#ifndef CLIENT_HPP_
#define CLIENT_HPP_

#include <iostream>
#include <map>
#include <Ice/Application.h>
#include "../generated/Bank.h"


namespace rozprochy
{
namespace lab4
{
namespace bank
{

class Client : public Ice::Application
{
private:
    Bank::SystemManagerPrx bank;
    typedef void (Client::*command)(std::istream&);

public:

    int run(int argc, char* argv[])
    {
        return 0;
    }
};

} /* namespace bank */
} /* namespace lab4 */
} /* namespace rozprochy */

#endif /* CLIENT_HPP_ */
