
#include <iostream>
#include "Pinger.hpp"

namespace rozprochy
{
namespace lab4
{
namespace bank
{

void Pinger::run()
{
    self = getThreadControl();
    //std::cout << "Pinger begins" << std::endl;
    while (true)
    {
        self.sleep(IceUtil::Time::milliSeconds(interval));
        {
            //std::cout << "Ping..." << std::endl;
            IceUtil::Mutex::Lock lock(mutex);
            if (! sid.empty())
            {
                if (!active)
                    break;
                bank->keepalive(sid);
            }
        }
    }
    //std::cout << "Pinger stops" << std::endl;
}

void Pinger::startPing(const std::string& sid)
{
    IceUtil::Mutex::Lock lock(mutex);
    this->sid = sid;
}

void Pinger::stopPing()
{
    IceUtil::Mutex::Lock lock(mutex);
    sid.clear();
}

void Pinger::terminate()
{
    IceUtil::Mutex::Lock lock(mutex);
    active = false;
}

} /* namespace bank */
} /* namespace lab4 */
} /* namespace rozprochy */
