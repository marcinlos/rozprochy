package rozprochy.lab2.common.game;

import rozprochy.lab2.common.GameMove;

public class ShootMove implements GameMove {

    public final Point point;
    
    public ShootMove(Point point) {
        this.point = point;
    }
    
    @Override
    public String toString() {
        return "[" + point.x + ", " + point.y + "]";
    }
}
