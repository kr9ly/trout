package net.kr9ly.trout;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Copyright 2015 kr9ly
 * <br />
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br />
 * http://www.apache.org/licenses/LICENSE-2.0
 * <br />
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class StateManager {

    public static StateManager newRoot() {
        return new StateManager(null);
    }

    private final StateManager parent;

    private final StateManagerChildren children = new StateManagerChildren(this);

    private final StateHolders holders = new StateHolders();

    private final StateCallbacksHolders callbacksHolders = new StateCallbacksHolders();

    private final StateInvocationHandler invocationHandler = new StateInvocationHandler(this);

    /* package */ StateManager(StateManager parent) {
        this.parent = parent;
    }

    public StateManager newChild() {
        return children.newChild();
    }

    public void commit() {
        holders.commit();
        children.commit();
        notifyUpdate(holders);
        postNotifyUpdate();
    }

    public <I> I getState(Class<I> stateClass) {
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{stateClass}, invocationHandler);
        return stateClass.cast(proxy);
    }

    public void subscribe(Class<?> stateClass, Object subscriber) {
        callbacksHolders.getCallbacks(stateClass).subscribe(subscriber);
    }

    public void remove() {
        if (parent != null) {
            parent.children.remove(this);
        }
    }

    public void storeToList(List<StateContainer> list) {
        holders.storeToList(list);
    }

    public void restoreFromList(List<StateContainer> list) {
        holders.restoreFromList(list);
    }

    /* package */ void notifyUpdate(StateHolders holders) {
        holders.notifyUpdate(this);
        children.notifyUpdate(holders);
    }

    /* package */ void postNotifyUpdate() {
        holders.postNotifyUpdate();
        children.postNotifyUpdate();
    }

    /* package */ void invokeCallbacks(Class<?> stateClass, String fieldKey) {
        callbacksHolders.getCallbacks(stateClass).getCallbacks(fieldKey).invoke();
        children.invokeCallbacks(stateClass, fieldKey);
    }

    /* package */ StateHolder getStateHolder(Class<?> stateClass, boolean readMode) {
        StateHolder holder = holders.getStateHolder(stateClass, readMode);
        if (holder == null && readMode && parent != null) {
            return parent.getStateHolder(stateClass, readMode);
        }
        return holder;
    }
}
