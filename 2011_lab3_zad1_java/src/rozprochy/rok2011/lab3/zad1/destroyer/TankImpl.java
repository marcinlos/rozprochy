package rozprochy.rok2011.lab3.zad1.destroyer;

public class TankImpl extends TankPOA {
    
    private String name;
    private Position position;
    
    public TankImpl(String name) {
        this.name = name;
        this.position = new Position(0, 0);
        System.out.printf("[Tank %s]: servant created\n", name);
    }

    @Override
    public Position position() {
        System.out.printf("[Tank %s]: get position\n", name);
        return position;
    }

    @Override
    public void position(Position p) throws InvalidPosition {
        System.out.printf("[Tank %s]: set position (%d, %d)\n", name, p.x, p.y);
        if (p.x > 100) {
            throw new InvalidPosition(InvalidPositionReason.out_of_range); 
        }
        position = p;
    }

    @Override
    public void destroy(String what) throws EntityNotFound, Indestructable,
            NotEnoughFirepower {
        System.out.printf("[Tank %s]: destroy %s", name, what);
        if (what.equals("math")) {
            throw new Indestructable();
        } else if (what.equals("earth")) {
            throw new NotEnoughFirepower(5, 10);
        } else if (what.equals("java")) {
            System.out.println("Java destroyed");
        } else {
            throw new EntityNotFound();
        }
    }

    @Override
    public String name() {
        System.out.printf("[Tank %s]: get name\n", name);
        return name;
    }

}
