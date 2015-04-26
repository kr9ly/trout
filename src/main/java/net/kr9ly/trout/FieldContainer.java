package net.kr9ly.trout;

import java.io.Serializable;

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
/* package */ class FieldContainer implements Serializable {

    private final String name;

    private final Class<? extends FieldHolder> holderClass;

    private final Serializable value;

    public FieldContainer(String name, Class<? extends FieldHolder> holderClass, Serializable value) {
        this.name = name;
        this.holderClass = holderClass;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public FieldHolder getFieldHolder() {
        try {
            FieldHolder holder = holderClass.newInstance();
            holder.setField(value);
            return holder;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
