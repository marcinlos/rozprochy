package rozprochy.lab4.bank.util;

public class PESEL {
    
    public static final int LENGTH = 11;
    
    private String value;

    public PESEL(String value) throws InvalidPESELException {
        if (value.length() != LENGTH) {
            throw new InvalidPESELException("Length is " + value.length() +
                    ", not " + LENGTH);
        }
        for (int i = 0; i < LENGTH; ++ i) {
            if (! isDecimal(value.charAt(i))) {
                throw new InvalidPESELException("Invalid character `" + 
                        value.charAt(i) + "' at char " + (i + 1));
            }
        }
        // TODO: Rest of validation
    }
    
    private static boolean isDecimal(char c) {
        return c >= '0' && c <= '9';
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public static boolean validate(String pesel) {
        try {
            new PESEL(pesel);
            return true;
        } catch (InvalidPESELException e) {
            return false;
        }
    }

}
