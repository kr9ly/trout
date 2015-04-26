package net.kr9ly.trout;

import java.io.Serializable;
import java.util.LinkedList;

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
/* package */ class StateContainer implements Serializable {

    private final Class<?> stateClass;

    private final LinkedList<FieldContainer> fields = new LinkedList<>();

    public StateContainer(Class<?> stateClass) {
        this.stateClass = stateClass;
    }

    public void add(FieldContainer field) {
        fields.add(field);
    }

    public Class<?> getStateClass() {
        return stateClass;
    }

    public LinkedList<FieldContainer> getFields() {
        return fields;
    }
}
