#include <iostream>
#include <omniORB4/CORBA.h>
#include "generated/greeter.h"

using namespace rozprochy::corba::test;

static const char TEST_SERVER_NAME[] = "StringService";



static CORBA::Object_ptr getObjectReference(CORBA::ORB_ptr orb,
        const char* objName)
{
    CosNaming::NamingContext_var rootContext;
    try
    {
        std::cout << "Waiting for naming service" << std::endl;
        // Obtain a reference to the root context of the Name service
        CORBA::Object_var obj = orb->resolve_initial_references("NameService");
        // Narrow the reference returned.
        rootContext = CosNaming::NamingContext::_narrow(obj);
        if (CORBA::is_nil(rootContext))
        {
            // Failed to narrow the root naming context
            return CORBA::Object::_nil();
        }
    }
    catch (const CORBA::ORB::InvalidName& ex)
    {
        // Service required is invalid [does not exist]
        return CORBA::Object::_nil();
    }
    catch (const CORBA::TRANSIENT& ex)
    {
        std::cerr << "Cannot find naming service" << std::endl;
        return CORBA::Object::_nil();
    }
    std::cout << "Got naming service" << std::endl;
    // Create a name object, containing the name test/context:
    CosNaming::Name name;
    name.length(1);
    name[0].id = CORBA::string_dup(objName);
    try
    {
        // Resolve the name to an object reference.
        return rootContext->resolve(name);
    }
    catch (const CosNaming::NamingContext::NotFound& ex)
    {
        // This exception is thrown if any of the components of the
        // path [contexts or the object] aren't found:
        std::cerr << "Couldn't find name: " << ex._name() << std::endl;
    }
    catch (const CORBA::COMM_FAILURE& ex)
    {
        // Caught system exception COMM_FAILURE -- unable to contact the naming service;
        std::cerr << "Communication failure: " << ex._name() << std::endl;
    }
    catch (const CORBA::SystemException& ex)
    {
        // Caught a CORBA::SystemException while using the naming service.
        std::cerr << "System exception: " << ex._name() << std::endl;
    }
    return CORBA::Object::_nil();
}


int main(int argc, char* argv[])
{
    CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);
    CORBA::Object_var obj = getObjectReference(orb, TEST_SERVER_NAME);
    if (! CORBA::is_nil(obj))
    {
        std::cout << "Obtained reference! :)" << std::endl;
        Greeter_var greeter_ref = Greeter::_narrow(obj);
        CORBA::String_var str = "Zbigniew";
        try
        {
            CORBA::String_var res = greeter_ref->greet(str);
            std::cout << "Result: " << res << std::endl;
        }
        catch (const CORBA::TRANSIENT& e)
        {
            std::cerr << "Fuck: " << e._name();
        }
    }
    else
    {
        std::cerr << "Failed to obtain valid service reference" << std::endl;
    }
    orb->destroy();
    return 0;
}


