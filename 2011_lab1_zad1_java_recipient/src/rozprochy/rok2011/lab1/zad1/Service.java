package rozprochy.rok2011.lab1.zad1;

/**
 * Generic intergral value transformer interface
 *  
 * @author los
 */
interface Service {
    byte process(byte value);

    short process(short value);

    int process(int value);

    long process(long value);
}