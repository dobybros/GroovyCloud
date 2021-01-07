package chat.utils.state;

public interface StateListener<K, T> {
    void stateChanged(K fromState, K toState, T t);
}
