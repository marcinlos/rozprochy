package rozprochy.lab2.common.game;

import rozprochy.lab2.common.MoveResult;

public class ShootResult implements MoveResult {

    public final boolean hit;
    
    public ShootResult(boolean hit) {
        this.hit = hit;
    }
    
    public static final ShootResult HIT = new ShootResult(true);
    public static final ShootResult MISS = new ShootResult(false);

}
