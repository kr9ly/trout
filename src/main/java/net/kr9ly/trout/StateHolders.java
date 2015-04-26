package net.kr9ly.trout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
/* package */ class StateHolders {

    private final Map<Class<?>, StateHolder> stateMap = new HashMap<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public void commit() {
        readLock.lock();
        try {
            for (StateHolder holder : stateMap.values()) {
                holder.commit();
            }
        } finally {
            readLock.unlock();
        }
    }

    public void notifyUpdate(StateManager stateManager) {
        readLock.lock();
        try {
            for (Map.Entry<Class<?>, StateHolder> entry : stateMap.entrySet()) {
                entry.getValue().notifyUpdate(stateManager);
            }
        } finally {
            readLock.unlock();
        }
    }

    public void postNotifyUpdate() {
        readLock.lock();
        try {
            for (Map.Entry<Class<?>, StateHolder> entry : stateMap.entrySet()) {
                entry.getValue().postNotifyUpdate();
            }
        } finally {
            readLock.unlock();
        }
    }

    public StateHolder getStateHolder(Class<?> stateClass, boolean readMode) {
        readLock.lock();
        try {
            StateHolder holder = stateMap.get(stateClass);
            if (holder == null) {
                if (readMode) {
                    return null;
                }
                readLock.unlock();
                writeLock.lock();
                try {
                    if (!stateMap.containsKey(stateClass)) {
                        holder = new StateHolder(stateClass);
                        stateMap.put(stateClass, holder);
                    } else {
                        holder = stateMap.get(stateClass);
                    }
                } finally {
                    readLock.lock();
                    writeLock.unlock();
                }
            }
            return holder;
        } finally {
            readLock.unlock();
        }
    }

    public void storeToList(List<StateContainer> list) {
        readLock.lock();
        try {
            for (Map.Entry<Class<?>, StateHolder> entry : stateMap.entrySet()) {
                list.add(entry.getValue().toStateContainer());
            }
        } finally {
            readLock.unlock();
        }
    }

    public void restoreFromList(List<StateContainer> list) {
        writeLock.lock();
        try {
            for (StateContainer container : list) {
                stateMap.put(container.getStateClass(), new StateHolder(container.getStateClass(), container.getFields()));
            }
        } finally {
            writeLock.unlock();
        }
    }
}
