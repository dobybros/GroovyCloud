package chat.utils.state;

public interface StateExecutor<K, T> {
    void execute(T t, StateMachine<K, T> stateMachine) throws Throwable;
}