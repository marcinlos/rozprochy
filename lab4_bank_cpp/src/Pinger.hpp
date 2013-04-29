/*
 * Pinger.hpp
 *
 *  Created on: Apr 29, 2013
 *      Author: los
 */

#ifndef PINGER_HPP_
#define PINGER_HPP_

#include <IceUtil/Thread.h>
#include "../generated/Bank.h"

namespace rozprochy
{
namespace lab4
{
namespace bank
{

class Pinger : public IceUtil::Thread
{
private:
    Bank::SystemManagerPrx bank;
    std::string sid;
    int interval;
    IceUtil::Mutex mutex;
    IceUtil::ThreadControl self;
    bool active;

public:

    const static int PING_PERIOD = 1500;

    Pinger(Bank::SystemManagerPrx bank, int interval = PING_PERIOD)
    : bank(bank), interval(interval)
    {
        active = true;
    }

    void startPing(const std::string& sid);

    void stopPing();

    void terminate();

    void run();

};

} /* namespace bank */
} /* namespace lab4 */
} /* namespace rozprochy */
#endif /* PINGER_HPP_ */
