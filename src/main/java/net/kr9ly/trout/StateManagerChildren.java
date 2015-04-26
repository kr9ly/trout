package net.kr9ly.trout;

import net.kr9ly.trout.StateManager;

import java.util.HashSet;
import java.util.Set;
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
/* package */ class StateManagerChildren {

    private final StateManager parent;

    private final Set<StateManager> children = new HashSet<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public StateManagerChildren(StateManager parent) {
        this.parent = parent;
    }

    public StateManager newChild() {
        writeLock.lock();
        try {
            StateManager child = new StateManager(parent);
            children.add(child);
            return child;
        } finally {
            writeLock.unlock();
        }
    }

    public void remove(StateManager child) {
        writeLock.lock();
        try {
            children.remove(child);
        } finally {
            writeLock.unlock();
        }
    }

    public void invokeCallbacks(Class<?> stateClass, String fieldKey) {
        readLock.lock();
        try {
            for (StateManager child : children) {
                if (child.getStateHolder(stateClass, true) == null) {
                    child.invokeCallbacks(stateClass, fieldKey);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void commit() {
        readLock.lock();
        try {
            for (StateManager child : children) {
                child.commit();
            }
        } finally {
            readLock.unlock();
        }
    }

    public void notifyUpdate(StateHolders holders) {
        readLock.lock();
        try {
            for (StateManager child : children) {
                child.notifyUpdate(holders);
            }
        } finally {
            readLock.unlock();
        }
    }

    public void postNotifyUpdate() {
        readLock.lock();
        try {
            for (StateManager child : children) {
                child.postNotifyUpdate();
            }
        } finally {
            readLock.unlock();
        }
    }
}
