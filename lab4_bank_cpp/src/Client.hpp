
#ifndef CLIENT_HPP_
#define CLIENT_HPP_

#include <iostream>
#include <map>
#include <Ice/Ice.h>
#include <Ice/Application.h>
#include <IceUtil/Handle.h>
#include "../generated/Bank.h"
#include "../generated/Users.h"
#include "Pinger.hpp"


namespace rozprochy
{
namespace lab4
{
namespace bank
{

class Client : public Ice::Application
{
private:
    Ice::CommunicatorPtr ice;
    Bank::SystemManagerPrx bank;
    IceUtil::Handle<Pinger> pinger;

    typedef void (Client::*command)(std::istream&);
    typedef std::map<std::string, command>::iterator map_iter;
    std::map<std::string, command> commands;

    // Properties
    std::string bank_endpoint;
    std::string prefix;
    std::string manager_name;

    std::string prompt;

    std::string session_id;
    std::string login;
    Ice::Context ctx;

    void load_properties_();
    void create_pinger_();

    Ice::ObjectPrx make_prx_(const std::string& name) const;
    Bank::AccountPrx get_account_() const;

    void repl_();
    void print_prompt_() const;
    void exit_gracefully_();

    void interpret_(const std::string& line);

    // Actual operations
    void register_(std::istream& in);
    void login_(std::istream& in);
    void logout_(std::istream& in);
    void balance_(std::istream& in);
    void deposit_(std::istream& in);
    void withdraw_(std::istream& in);
    void spam_(std::istream& in);

    bool check_logged_() const;

    void begin_session_(const std::string& sid);
    void session_terminated_();

public:

    Client();

    int run(int argc, char* argv[]);

    void interruptCallback(int);
};

} /* namespace bank */
} /* namespace lab4 */
} /* namespace rozprochy */

#endif /* CLIENT_HPP_ */
