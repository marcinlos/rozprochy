package rozprochy.lab3.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.omg.CORBA.ARG_IN;
import org.omg.CORBA.ARG_INOUT;
import org.omg.CORBA.ARG_OUT;
import org.omg.CORBA.Any;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Request;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;

public class DIIParser {
    
    private Iterator<String> input;
    private Object object;
    
    private ORB orb = ORB.init();
    private String operation;
    private NamedValue resultVal;
    private NVList argList;
    
    private static final Map<String, Filler> fillers = 
            new HashMap<String, Filler>();
    
    private static TypeCode getTC(TCKind kind) {
        return ORB.init().get_primitive_tc(kind);
    }
    
    {
        fillers.put("long", new Filler() {
            @Override public void fill(Any a) {
                a.type(getTC(TCKind.tk_long));
            }
            @Override public void fill(Any a, String value) {
                a.insert_long(Integer.valueOf(value));
            }
        });
        fillers.put("longlong", new Filler() {
            @Override public void fill(Any a) {
                a.type(getTC(TCKind.tk_longlong));
            }
            @Override public void fill(Any a, String value) {
                a.insert_longlong(Long.valueOf(value));
            }
        });
        fillers.put("string", new Filler() {
            @Override public void fill(Any a) {
                a.type(getTC(TCKind.tk_string));
            }
            @Override public void fill(Any a, String value) {
                a.insert_string(value);
            }
        });
        fillers.put("float", new Filler() {
            @Override public void fill(Any a) {
                a.type(getTC(TCKind.tk_float));
            }
            @Override public void fill(Any a, String value) {
                a.insert_float(Float.valueOf(value));
            }
        });
        fillers.put("double", new Filler() {
            @Override public void fill(Any a) {
                a.type(getTC(TCKind.tk_double));
            }
            @Override public void fill(Any a, String value) {
                a.insert_double(Double.valueOf(value));
            }
        });
        fillers.put("bool", new Filler() {
            @Override public void fill(Any a) {
                a.type(getTC(TCKind.tk_boolean));
            }
            @Override public void fill(Any a, String value) {
                a.insert_boolean(Boolean.valueOf(value));
            }
        });
        fillers.put("void", new Filler() {
            @Override public void fill(Any a) {
                a.type(getTC(TCKind.tk_void));
            }
            @Override public void fill(Any a, String v) throws DIIException {
                throw new DIIException("Cannot specify value for void type");
            }
        });
    }

    private interface Filler {
        void fill(Any a, String value) throws DIIException;
        void fill(Any a);
    }
    
    public DIIParser(Iterator<String> input, Object object) {
        this.input = input;
        this.object = object;
    }
    
    public Request create() throws DIIException {
        try {
            operation = input.next();
            createReturnValue();
            createArgs();
            return object._create_request(null, operation, argList, resultVal);
        } catch (NoSuchElementException e) {
            throw new DIIException();
        }
    }
    
    private static void mustFillAny(Any a, String type) throws DIIException {
        Filler filler = fillers.get(type);
        if (filler != null) {
            filler.fill(a);
        } else {
            throw new DIIException("Invalid type `" + type + "'");
        }
    }
    
    private void createReturnValue() throws DIIException {
        Any result = orb.create_any();
        String type = input.next();
        mustFillAny(result, type);
    }
    
    private void createArgs() throws DIIException {
        argList = orb.create_list(2);
        while (input.hasNext()) {
            processArg();
        }
    }
    
    private void processArg() throws DIIException {
        Any arg = orb.create_any();
        int argDir = toDir(input.next());
        String type = input.next();
        Filler filler = fillers.get(type);
        if (filler != null) {
            if (argDir == ARG_OUT.value) {
                filler.fill(arg);
            } else {
                filler.fill(arg, input.next());
            }
            String name = "arg" + argList.count();
            argList.add_value(name, arg, argDir);
        } else {
            throw new DIIException("Invalid type `" + type + "'");
        }
    }
    
    private static int toDir(String s) throws DIIException {
        if (s.equals("in")) {
            return ARG_IN.value;
        } else if (s.equals("out")) {
            return ARG_OUT.value;
        } else if (s.equals("inout")) {
            return ARG_INOUT.value;
        } else {
            throw new DIIException("Invalid direction `" + s + "'");
        }
    }
        
}
