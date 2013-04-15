package rozprochy.lab2.common.game;

import rozprochy.lab2.common.GameUpdate;

public class EnemyShootUpdate implements GameUpdate {

    public final Point point;
    public final boolean result;
    
    public EnemyShootUpdate(Point point, boolean result) {
        this.point = point;
        this.result = result;
    }

}
