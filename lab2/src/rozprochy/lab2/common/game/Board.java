package rozprochy.lab2.common.game;

import java.io.Serializable;

public class Board implements Serializable {
    
    public static final int SIZE = 10;
    
    public int[][] fields = new int[SIZE][SIZE];
    
}
