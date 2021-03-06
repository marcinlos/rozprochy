package rozprochy.lab3.common;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNaming.NamingContextPackage.NotFoundReason;

public class CORBAUtil {
    
    private CORBAUtil() { }

    /**
     * Formats the name given by array of components using slash (/) as
     * a separator. Doesn't use name service object.
     */
    public static String formatName(NameComponent[] name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length; ++ i) {
            sb.append(name[i].id);
            if (i != name.length - 1) {
                sb.append('/');
            }
        }
        return sb.toString();
    }

    /**
     * Creates a message for {@code NotFound} exception thrown by the name 
     * service. It's kind of a mess, hence a separate function.
     */
    public static String formatNotFoundReason(NotFound e) {
        // Cannot switch, IDL enums are not mapped to enums/integral values
        NameComponent[] rest = e.rest_of_name;
        String first = rest[0].id;
        StringBuilder reason = new StringBuilder();
        
        if (e.why == NotFoundReason.missing_node) {
            reason.append("Node `").append(first)
                .append("' missing in the naming context tree");
        } else if (e.why == NotFoundReason.not_context) {
            reason.append("Node `").append(first)
                .append("' is an object, naming context was expected");
        } else if (e.why == NotFoundReason.not_object) {
            reason.append("Node `").append(first)
            .append("' is a naming context, object was expected");
        }
        return reason.toString();
    }
    
    public static String anyToString(Any any) {
        ORB orb = ORB.init();
        TypeCode tc = any.type();
        if (tc.equal(orb.get_primitive_tc(TCKind.tk_boolean))) {
            return String.valueOf(any.extract_boolean()) + " (bool)";
        } else if (tc.equal(orb.get_primitive_tc(TCKind.tk_long))) {
            return String.valueOf(any.extract_long()) + " (long)";
        } else if (tc.equal(orb.get_primitive_tc(TCKind.tk_longlong))) {
            return String.valueOf(any.extract_longlong()) + " (longlong)";
        } else if (tc.equal(orb.get_primitive_tc(TCKind.tk_string))) {
            return "\"" + any.extract_string() + "\"";
        } else if (tc.equal(orb.get_primitive_tc(TCKind.tk_short))) {
            return String.valueOf(any.extract_short());
        } else if (tc.equal(orb.get_primitive_tc(TCKind.tk_float))) {
            return String.valueOf(any.extract_float()) + " (float)";
        } else if (tc.equal(orb.get_primitive_tc(TCKind.tk_double))) {
            return String.valueOf(any.extract_double()) + " (double)";
        } else if (tc.equal(orb.get_primitive_tc(TCKind.tk_void))) {
            return "(void)";
        } else {
            return "(?)";
        }
    }
    
}
