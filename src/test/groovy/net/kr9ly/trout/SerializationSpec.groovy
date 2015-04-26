package net.kr9ly.trout

import org.apache.commons.lang3.SerializationUtils
import spock.lang.Specification

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
class SerializationSpec extends Specification {

    def "serialize states"() {
        setup:
        def manager = StateManager.newRoot()
        def manager2 = StateManager.newRoot()

        when:
        def state = manager.getState(TestState.class)
        state.foo = foo
        state.bar = bar
        manager.commit()

        def list = new LinkedList<StateContainer>()
        manager.storeToList(list)

        manager2.restoreFromList(SerializationUtils.clone(list))

        def updated = manager2.getState(TestState.class)
        manager2.commit()

        then:
        updated.foo == foo
        updated.bar == bar

        where:
        foo | bar
        "a" | 1
    }

    private interface TestState {

        @Getter("foo")
        String getFoo()

        @Setter("foo")
        void setFoo(String foo)

        @Getter("bar")
        int getBar()

        @Setter("bar")
        void setBar(int bar)
    }
}
