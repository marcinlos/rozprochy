#include <iostream>
#include <iomanip>
#include "Client.hpp"
#include "Scanner.hpp"


namespace rozprochy
{
namespace lab4
{
namespace bank
{

Client::Client()
: prompt("> ")
{
    commands["register"] = &Client::register_;
    commands["login"]    = &Client::login_;
    commands["logout"]   = &Client::logout_;
    commands["balance"]  = &Client::balance_;
    commands["deposit"]  = &Client::deposit_;
    commands["withdraw"] = &Client::withdraw_;
    commands["spam"]     = &Client::spam_;
}

int Client::run(int argc, char* argv[])
{
    ice = communicator();
    load_properties_();
    std::cout << "Obtaining bank reference..." << std::flush;
    Ice::ObjectPrx obj = make_prx_(manager_name);
    try
    {
        bank = Bank::SystemManagerPrx::checkedCast(obj);
        std::cout << "done" << std::endl;
    }
    catch (const Ice::ConnectionRefusedException& e)
    {
        std::cerr << "\nConnection refused, is server running?" << std::endl;
        return 1;
    }
    callbackOnInterrupt();
    create_pinger_();
    repl_();
    exit_gracefully_();
    return 0;
}

void Client::load_properties_()
{
    Ice::PropertiesPtr prop = ice->getProperties();
    bank_endpoint = prop->getProperty("Bank.Endpoints");
    prefix = prop->getProperty("Bank.Prefix");
    manager_name = prop->getProperty("Bank.Name");
}

void Client::create_pinger_()
{
    pinger = new Pinger(bank);
    pinger->start();
}

Ice::ObjectPrx Client::make_prx_(const std::string& name) const
{
    std::string str = prefix + "/" + name + bank_endpoint;
    return ice->stringToProxy(str);
}

Bank::AccountPrx Client::get_account_() const
{
    if (! session_id.empty())
    {
        Ice::ObjectPrx proxy = make_prx_(session_id);
        return Bank::AccountPrx::checkedCast(proxy);
    }
    else
    {
        // nullary proxy
        return Bank::AccountPrx();
    }
}

void Client::repl_()
{
    std::string line;
    print_prompt_();
    while (std::getline(std::cin, line))
    {
        interpret_(line);
        print_prompt_();
    }
}

void Client::print_prompt_() const
{
    std::cout << prompt << std::flush;
}


void Client::interpret_(const std::string& line)
{
    std::istringstream ss(line);
    std::string cmd;
    ss >> cmd;
    if (! cmd.empty())
    {
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
            std::cout << "Unknown command `" << cmd << "'" << std::endl;
        }
    }
}

void Client::exit_gracefully_()
{
    std::cout << '\r' << std::flush;
    pinger->terminate();
    if (! session_id.empty())
    {
        try
        {
            bank->logout(session_id);
        }
        catch (const Users::SessionException&)
        {
            std::cerr << "Invalid session" << std::endl;
        }
    }
}

void Client::begin_session_(const std::string& sid)
{
    session_id = sid;
    pinger->startPing(sid);
}

void Client::session_terminated_()
{
    session_id.clear();
    pinger->stopPing();
}

void Client::interruptCallback(int)
{
    std::cout << '\r' << std::flush;
    pinger->terminate();
    ice->destroy();
}

bool Client::check_logged_() const
{
    if (! bank)
    {
        std::cerr << "Not connected!" << std::endl;
        return false;
    }
    else if (session_id.empty())
    {
        std::cerr << "Not logged in!" << std::endl;
        return false;
    }
    else return true;
}


void Client::register_(std::istream& in)
{
    try
    {
        Scanner input(in);
        std::string pesel = input.next();
        std::string password = input.next();
        bank->createAccount(pesel, password);
        std::cout << "Account successfully created" << std::endl;
    }
    catch (const ParseError&)
    {
        std::cerr << "Usage: register <pesel> <password>" << std::endl;
    }
    catch (const Users::InvalidLogin& e)
    {
        std::cerr << "Invalid login: " << e.reason << std::endl;
    }
    catch (const Users::InvalidPassword& e)
    {
        std::cerr << "Invalid password: " << e.reason << std::endl;
    }
    catch (const Users::DbError&)
    {
        std::cerr << "Server database error" << std::endl;
    }
}

void Client::login_(std::istream& in)
{
    try
    {
        Scanner input(in);
        std::string pesel = input.next();
        std::string password = input.next();
        std::string sid = bank->login(pesel, password);
        std::cout << "Logged in, sid=" << sid << std::endl;
        begin_session_(sid);
    }
    catch (const ParseError&)
    {
        std::cerr << "Usage: login <pesel> <password>" << std::endl;
    }
    catch (const Users::AuthenticationFailed&)
    {
        std::cerr << "Invalid login or password" << std::endl;
    }
    catch (const Users::MultiLogin&)
    {
        std::cerr << "User already logged in" << std::endl;
    }
    catch (const Users::LoginException& e)
    {
        std::cerr << e.what() << std::endl;
    }
    catch (const Users::DbError&)
    {
        std::cerr << "Server database error" << std::endl;
    }
}

void Client::logout_(std::istream& in)
{
    if (check_logged_())
    {
        try
        {
            bank->logout(session_id);
            session_terminated_();
        }
        catch (const Users::SessionException&)
        {
            std::cerr << "Invalid session" << std::endl;
            session_terminated_();
        }
    }
}

void Client::balance_(std::istream& in)
{
    if (check_logged_())
    {
        try
        {
            Bank::AccountPrx account = get_account_();
            int balance = account->getBalance();
            std::cout << "Account : " << std::setw(10) << balance
                      << ".00 $" << std::endl;
        }
        catch (const Users::SessionException&)
        {
            std::cerr << "Invalid session" << std::endl;
            session_terminated_();
        }
        catch (const Bank::OperationException&)
        {
            std::cerr << "Operation exception" << std::endl;
        }
        catch (const Users::DbError&)
        {
            std::cerr << "Server database error" << std::endl;
        }
    }
}

void Client::deposit_(std::istream& in)
{
    if (check_logged_())
    {
        try
        {
            Scanner input(in);
            int amount = input.nextInt();
            Bank::AccountPrx account = get_account_();
            account->deposit(amount);
        }
        catch (const ParseError&)
        {
            std::cerr << "Usage: deposit <amount>" << std::endl;
        }
        catch (const Users::SessionException&)
        {
            std::cerr << "Invalid session" << std::endl;
            session_terminated_();
        }
        catch (const Bank::OperationException&)
        {
            std::cerr << "Operation exception" << std::endl;
        }
        catch (const Users::DbError&)
        {
            std::cerr << "Server database error" << std::endl;
        }
    }
}

void Client::withdraw_(std::istream& in)
{
    if (check_logged_())
    {
        try
        {
            Scanner input(in);
            int amount = input.nextInt();
            Bank::AccountPrx account = get_account_();
            account->withdraw(amount);
        }
        catch (const ParseError&)
        {
            std::cerr << "Usage: withdraw <amount>" << std::endl;
        }
        catch (const Users::SessionException&)
        {
            std::cerr << "Invalid session" << std::endl;
            session_terminated_();
        }
        catch (const Bank::OperationException&)
        {
            std::cerr << "Operation exception" << std::endl;
        }
        catch (const Users::DbError&)
        {
            std::cerr << "Server database error" << std::endl;
        }
    }
}

void Client::spam_(std::istream& in)
{
    if (check_logged_())
    {
        std::cerr << "Sorry, not implemented in C++ version." << std::endl;
    }
}




} /* namespace bank */
} /* namespace lab4 */
} /* namespace rozprochy */
