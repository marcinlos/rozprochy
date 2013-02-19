package rozprochy.corba.test.server;

import rozprochy.corba.test.GreeterPOA;


public class GreeterImpl extends GreeterPOA {

    @Override
    public String greet(String name) {
        return "Hello " + name;
    }

}
