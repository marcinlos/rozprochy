package rozprochy.lab2.common.game;

import java.io.Serializable;

public class Point implements Serializable {
    
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
}
