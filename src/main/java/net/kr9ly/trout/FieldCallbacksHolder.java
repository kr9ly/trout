package net.kr9ly.trout;

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
/* package */ class FieldCallbacksHolder {

    private final Set<CallbackHolder> callbacks = new HashSet<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public void add(CallbackHolder holder) {
        writeLock.lock();
        try {
            callbacks.add(holder);
        } finally {
            writeLock.unlock();
        }
    }

    public void invoke() {
        readLock.lock();
        try {
            for (CallbackHolder holder : callbacks) {
                holder.invoke();
            }
        } finally {
            readLock.unlock();
        }
    }
}
