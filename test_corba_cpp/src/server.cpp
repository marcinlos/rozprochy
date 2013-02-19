#include <iostream>
#include <omniORB4/CORBA.h>
#include "Greeter_impl.h"

using namespace rozprochy::corba::test::impl;

static const char TEST_SERVER_NAME[] = "StringService";



int main(int argc, char* argv[])
{
    CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);

    CORBA::Object_var obj = orb->resolve_initial_references("RootPOA");
    PortableServer::POA_var poa = PortableServer::POA::_narrow(obj);
    PortableServer::POAManager_var pman = poa->the_POAManager();
    pman->activate();

    Greeter_impl* greeter = new Greeter_impl;
    PortableServer::ObjectId_var ref = poa->activate_object(greeter);

    obj = greeter->_this();

    CORBA::Object_var ncObj = orb->resolve_initial_references("NameService");
    CosNaming::NamingContext_var nc = CosNaming::NamingContext::_narrow(ncObj);

    CosNaming::Name contextName;
    contextName.length(1);
    contextName[0].id = TEST_SERVER_NAME;
    // Not really sure how is this to be used, setting arbitrarily caused
    // server error
    // contextName[0].kind = "???";

    nc->rebind(contextName, obj);

    greeter->_remove_ref();

    orb->run();
    orb->destroy();
    return 0;
}
