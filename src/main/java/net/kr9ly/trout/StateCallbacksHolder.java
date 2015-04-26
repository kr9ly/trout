package net.kr9ly.trout;

import java.lang.reflect.Method;
import java.util.HashMap;
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
/* package */ class StateCallbacksHolder {

    private final Map<String, FieldCallbacksHolder> callbacks = new HashMap<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public void subscribe(Object subscriber) {
        for (Method method : subscriber.getClass().getMethods()) {
            OnUpdate onUpdate = method.getAnnotation(OnUpdate.class);
            if (onUpdate != null) {
                getCallbacks(onUpdate.value()).add(new CallbackHolder(subscriber, method));
            }
        }
    }

    public FieldCallbacksHolder getCallbacks(String key) {
        readLock.lock();
        try {
            FieldCallbacksHolder holders = callbacks.get(key);
            if (holders == null) {
                readLock.unlock();
                writeLock.lock();
                try {
                    if (!callbacks.containsKey(key)) {
                        holders = new FieldCallbacksHolder();
                        callbacks.put(key, holders);
                    } else {
                        holders = callbacks.get(key);
                    }
                } finally {
                    readLock.lock();
                    writeLock.unlock();
                }
            }
            return holders;
        } finally {
            readLock.unlock();
        }
    }
}
