#ifndef GREETERIMPL_H_
#define GREETERIMPL_H_

#include "generated/greeter.h"


namespace rozprochy
{
namespace corba
{
namespace test
{
namespace impl
{

    class Greeter_impl
    : public POA_rozprochy::corba::test::Greeter,
      public PortableServer::RefCountServantBase
    {
    public:
        virtual char* greet(const char* name);
    };

} /* namespace impl */
} /* namespace test */
} /* namespace corba */
} /* namespace rozprochy */
#endif /* GREETERIMPL_H_ */
