package rozprochy.rok2012.lab1.zad4.fsm;

import java.util.HashMap;
import java.util.Map;

/**
 * @param <S> Type of state
 * @param <L> Type of state labels
 * @param <T> Type of transition labels
 */
public class FSM<S extends State, L, T> {
    
    private Map<L, S> states = new HashMap<L, S>();
    private Map<L, Map<T, L>> transitions = new HashMap<L, Map<T,L>>();
    
    private S currentState;
    private L currentLabel;
    

    public void addInitialState(L label, S state) {
        addState(label, state);
        currentState = state;
        currentLabel = label;
    }
    
    public void addState(L label, S state) {
        states.put(label, state);
        ensureEntryExists(label);
    }
    
    public void addTransition(L source, T edge, L target) {
        ensureEntryExists(source);
        Map<T, L> edges = transitions.get(source);
        edges.put(edge, target);
    }
    
    public synchronized void advance(T edge) {
        Map<T, L> edges = transitions.get(currentLabel);
        if (edges != null) {
            L target = edges.get(edge);
            if (target != null) {
                currentState.onEnd();
                currentState = states.get(target);
                currentLabel = target;
                currentState.onBegin();
                return;
            }
        }
        String message = "From: " + currentState.toString() + " by " + edge;
        throw new MissingTransitionException(message);
    }
    
    public synchronized S getCurrentState() {
        return currentState;
    }
    
    public synchronized L getCurrentStateLabel() {
        return currentLabel;
    }
    
    private void ensureEntryExists(L label) {
        if (! transitions.containsKey(label)) {
            transitions.put(label, new HashMap<T, L>());
        }
    }
    
}
