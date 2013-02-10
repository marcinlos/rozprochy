package rozprochy.rok2011.lab1.zad1;

/**
 * Transformer incrementing its input value
 *
 * @author los
 */
class Incrementor implements Service {

    @Override
    public byte process(byte value) {
        return (byte) (value + 1);
    }

    @Override
    public short process(short value) {
        return (short) (value + 1);
    }

    @Override
    public int process(int value) {
        return value + 1;
    }

    @Override
    public long process(long value) {
        return value + 1;
    }
    
}