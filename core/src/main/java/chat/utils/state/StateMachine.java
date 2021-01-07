package chat.utils.state;


import chat.logs.LoggerEx;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class StateMachine<K, T> {
    private static final String TAG = StateMachine.class.getSimpleName();
    // 声明这个状态机的实例化类
    T t;
    // 当前状态
    private K currentState;
    // 状态所对应的处理器
    private ConcurrentHashMap<K, State<K, T>> stateMap = new ConcurrentHashMap<>();
    private boolean stateMachineStated = false;
    // 发生错误时的处理器（eg：改变state时失败）
    private StateErrorOccurredExecutor<K, T> stateErrorOccurredExecutor;
    // state变化后的监听器
    private CopyOnWriteArrayList<StateListener<K, T>> stateListeners;
    private String name;

    public StateMachine(String name, K initialState, T t) {
        this.currentState = initialState;
        this.t = t;
        this.name = name;
    }

    public StateMachine<K, T> addStateListener(StateListener<K, T> stateListener) {
        if(stateListeners == null) {
            synchronized (this) {
                if(stateListeners == null) {
                    stateListeners = new CopyOnWriteArrayList<>();
                }
            }
        }
        if(!stateListeners.contains(stateListener)) {
            stateListeners.add(stateListener);
        }
        return this;
    }

    public boolean removeStateListener(StateListener<K, T> stateListener) {
        if(stateListeners != null) {
            return stateListeners.remove(stateListener);
        }
        return false;
    }

    public State<K, T> execute() {
        return new State<K, T>(null);
    }

    // 到达当前状态的回调
    public State<K, T> execute(StateExecutor<K, T> stateExecutor) {
        return new State<K, T>(stateExecutor);
    }

    public StateExecutor<K, T> newExecutor(StateExecutor<K, T> stateExecutor) {
        return stateExecutor;
    }

    public synchronized void restart() {
        stateMachineStated = false;
//        currentState = null;
        changeState(null, "restarted");
    }

    public synchronized void reset() {
        restart();
        stateMap.clear();
    }

    public StateMachine<K, T> configState(K state, State<K, T> stateObj) {
        if(stateMachineStated)
            throw new IllegalStateException(name + ": StateMachine is already started, can not config states now, please config states before start. currentState " + currentState + " obj " + t);
        if(stateObj != null && state != null) {
            stateObj.setState(state);
            stateMap.put(state, stateObj);
        }
        return this;
    }

    public StateMachine<K, T> errorOccurred(StateErrorOccurredExecutor<K, T> stateErrorOccurredExecutor) {
        this.stateErrorOccurredExecutor = stateErrorOccurredExecutor;
        return this;
    }

    private void changeState(K toState, String message) {
        LoggerEx.info(TAG, name + ": [changeState] " + message);
        State<K, T> currentStateObj = stateMap.get(currentState);
        StateExecutor<K, T> leaveStateExecutor = null;
        if (currentStateObj != null)
            leaveStateExecutor = currentStateObj.getLeaveStateExecutor();
        K old = currentState;
        currentState = toState;
        if(leaveStateExecutor != null) {
            try {
                leaveStateExecutor.execute(t, this);
            } catch(Throwable t) {
                LoggerEx.error(TAG, name + ": Leave state from " + old + " to " + toState + " failed, " + t.getMessage() + " for leaveStateExecutor " + leaveStateExecutor);
            }
        }
        if(stateListeners != null) {
            stateListeners.forEach(stateListener -> {
                try {
                    stateListener.stateChanged(old, toState, t);
                } catch(Throwable t) {
                    LoggerEx.error(TAG, name + ": State changed callback failed, " + t.getMessage() + " for listener " + stateListener);
                }
            });
        }
    }
    public synchronized void gotoState(K state, String reason) {
        gotoState(state, reason, null);
    }

    public synchronized void gotoState(K state, String reason, StateBeforeExecutor<K, T> stateBeforeExecutor) {
        if(!stateMachineStated)
            stateMachineStated = true;
        State<K, T> stateObj = stateMap.get(state);
        if(stateObj == null) {
            IllegalArgumentException throwable = new IllegalArgumentException(name + ": State " + state + " is not configured, reason " + reason + " for obj " + t);
            if(stateErrorOccurredExecutor != null) {
                LoggerEx.error(TAG, name + ": go to state " + state + ", stateObj not exist");
                try {
                    stateErrorOccurredExecutor.onError(throwable, currentState, state, this.t, this);
                } catch (Throwable t1) {
                    LoggerEx.error(TAG, "Execute state occurred error executor failed, [" + t1.getMessage() + "] state " + state + " from state " + currentState + " obj " + t + " reason " + reason);
                }
                return;
            } else {
                throw throwable;
            }
        }

        Set<K> gotoStates = null;
        if(currentState != null) {
            State<K, T> currentStateObj = stateMap.get(currentState);
            if(currentStateObj == null) {
                IllegalStateException throwable = new IllegalStateException(name + ": CurrentState is illegal " + currentState + " maybe caused by modifying state configuration during running the state machine. Force currentState to be null. obj " + t + " reason " + reason);
                if(stateErrorOccurredExecutor != null) {
                    LoggerEx.error(TAG, name + ": go to state " + state + ", currentStateObj not exist");
                    try {
                        stateErrorOccurredExecutor.onError(throwable, currentState, state, this.t, this);
                    } catch (Throwable t1) {
                        LoggerEx.error(TAG, "Execute state occurred error executor failed, [" + t1.getMessage() + "] state " + state + " from state " + currentState + " obj " + t + " reason " + reason);
                    }
                    return;
                } else {
                    throw throwable;
                }
            } else {
                gotoStates = currentStateObj.getGotoStates();
            }
        }

        if(gotoStates != null) {
            if(!gotoStates.contains(state)) {
                IllegalStateException throwable = new IllegalStateException(name + ": Current state is " + currentState + ", can NOT go to state " + state + " obj " + t + " reason " + reason);
                if(stateErrorOccurredExecutor != null) {
                    LoggerEx.error(TAG, name + ": go to state " + state + ", can gotoStates not contains state, gotoStates: " + gotoStates);
                    try {
                        stateErrorOccurredExecutor.onError(throwable, currentState, state, this.t, this);
                    } catch (Throwable t1) {
                        LoggerEx.error(TAG, "Execute state occurred error executor failed, [" + t1.getMessage() + "] state " + state + " from state " + currentState + " obj " + t + " reason " + reason);
                    }
                    return;
                } else {
                    throw throwable;
                }
            }
        }

        StateExecutor<K, T> executor = stateObj.getStateExecutor();
        K lastState = currentState;
        changeState(state, "StateMachine currentState " + currentState + " goes to " + state + " successfully. reason " + reason + " obj " + t);
        if(executor != null) {
            try {
                if(stateBeforeExecutor != null) {
                    stateBeforeExecutor.execute(this.t, this);
                }
                executor.execute(this.t, this);
            } catch(Throwable t) {
                t.printStackTrace();
                if(stateErrorOccurredExecutor != null) {
                    LoggerEx.error(TAG, name + ": Execute state executor failed, [" + t.getMessage() + "] state " + state + " from state " + lastState + " obj " + t + " reason " + reason + " will invoke stateErrorOccurredExecutor#onError method");
                    try {
                        stateErrorOccurredExecutor.onError(t, lastState, state, this.t, this);
                    } catch (Throwable t1) {
//                        currentState = lastState;
                        changeState(lastState, "Execute state occurred error executor failed, [" + t1.getMessage() + "] state " + state + " from state " + lastState + " obj " + t + " reason " + reason + " will change back to last state " + lastState);
                    }
                } else {
                    changeState(lastState, "Execute state executor failed, [" + t.getMessage() + "] state " + state + " from state " + lastState + " obj " + t + " reason " + reason + " will change back to last state " + lastState + " because no stateErrorOccurredExecutor");
                }
            }
        }
    }

    public K getCurrentState() {
        return currentState;
    }
}
