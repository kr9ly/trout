package net.kr9ly.trout;

import java.io.*;
import java.util.Objects;
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
/* package */ class ObjectHolder implements FieldHolder {

    private Serializable committed;

    private transient Serializable dirty;

    private transient byte[] serialized;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private volatile boolean isUpdated = false;

    private volatile boolean needsNotify = false;

    @Override
    public Serializable getField() {
        readLock.lock();
        try {
            return committed;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setField(Serializable object) {
        this.dirty = object;
        readLock.lock();
        try {
            if (Objects.equals(dirty, committed)) {
                isUpdated = false;
                return;
            }
            isUpdated = true;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void commit() {
        writeLock.lock();
        try {
            if (isUpdated) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(dirty);
                serialized = baos.toByteArray();
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized));
                committed = (Serializable) ois.readObject();
                needsNotify = true;
            }
            isUpdated = false;
        } catch (IOException | ClassNotFoundException e) {
            throw new Error(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void markNotifyFinished() {
        needsNotify = false;
    }

    @Override
    public boolean isUpdated() {
        return isUpdated;
    }

    @Override
    public boolean needsNotify() {
        return needsNotify;
    }

    @Override
    public FieldContainer toFieldContainer(String key) {
        return new FieldContainer(key, ObjectHolder.class, committed);
    }
}
