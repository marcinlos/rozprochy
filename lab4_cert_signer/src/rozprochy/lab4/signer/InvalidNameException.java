package rozprochy.lab4.signer;

public class InvalidNameException extends Exception {

    private String name;
    
    public InvalidNameException(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Invalid name: `" + name + "' (not .csr)";
    }
    
}
