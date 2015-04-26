package net.kr9ly.trout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
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
/* package */ class StateHolder {

    private final Class<?> stateClass;

    private final Map<String, FieldHolder> fields = new HashMap<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public StateHolder(Class<?> stateClass) {
        this.stateClass = stateClass;
    }

    public StateHolder(Class<?> stateClass, LinkedList<FieldContainer> list) {
        this.stateClass = stateClass;
        for (FieldContainer container : list) {
            fields.put(container.getName(), container.getFieldHolder());
        }
    }

    public Serializable get(String key) {
        return getFieldHolder(key).getField();
    }

    public void set(String key, Serializable object) {
        getFieldHolder(key).setField(object);
    }

    public void commit() {
        readLock.lock();
        try {
            for (FieldHolder holder : fields.values()) {
                holder.commit();
            }
        } finally {
            readLock.unlock();
        }
    }

    public void notifyUpdate(StateManager stateManager) {
        readLock.lock();
        try {
            for (Map.Entry<String, FieldHolder> entry : fields.entrySet()) {
                String key = entry.getKey();
                FieldHolder holder = entry.getValue();
                if (holder.needsNotify()) {
                    stateManager.invokeCallbacks(stateClass, key);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void postNotifyUpdate() {
        readLock.lock();
        try {
            for (Map.Entry<String, FieldHolder> entry : fields.entrySet()) {
                FieldHolder holder = entry.getValue();
                holder.markNotifyFinished();
            }
        } finally {
            readLock.unlock();
        }
    }

    private FieldHolder getFieldHolder(String key) {
        readLock.lock();
        try {
            FieldHolder holder = fields.get(key);
            if (holder == null) {
                readLock.unlock();
                writeLock.lock();
                try {
                    if (!fields.containsKey(key)) {
                        holder = new ObjectHolder();
                        fields.put(key, holder);
                    } else {
                        holder = fields.get(key);
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

    public StateContainer toStateContainer() {
        StateContainer container = new StateContainer(stateClass);
        for (Map.Entry<String, FieldHolder> entry : fields.entrySet()) {
            container.add(entry.getValue().toFieldContainer(entry.getKey()));
        }
        return container;
    }
}
