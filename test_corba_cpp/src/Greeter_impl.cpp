#include <cstring>
#include <cstdio>
#include "Greeter_impl.h"

namespace rozprochy
{
namespace corba
{
namespace test
{
namespace impl
{

    char* Greeter_impl::greet(const char* name)
    {
        // Freeing is the caller's responsibility - skeleton code
        // frees this memory. CORBA-functions for memory management
        // due to alleged 'nonportability' of new/delete.
        std::size_t n = std::strlen(name) + 8;
        char* buffer = CORBA::string_alloc(n);
        std::snprintf(buffer, n, "Hello %s!", name);
        return buffer;
    }

} /* namespace impl */
} /* namespace test */
} /* namespace corba */
} /* namespace rozprochy */
