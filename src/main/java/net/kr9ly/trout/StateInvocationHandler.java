package net.kr9ly.trout;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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
/* package */ class StateInvocationHandler implements InvocationHandler {

    private StateManager stateManager;

    StateInvocationHandler(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> stateClass = proxy.getClass().getInterfaces()[0];
        Getter getter = method.getAnnotation(Getter.class);
        if (getter != null) {
            if (args != null) {
                throw new IllegalStateException("Getter method must have no arguments.");
            }
            StateHolder holder = stateManager.getStateHolder(stateClass, true);
            if (holder == null) {
                Class<?> returnType = method.getReturnType();
                if (!returnType.isPrimitive()) {
                    return null;
                }
                if (returnType.isAssignableFrom(int.class)
                        || returnType.isAssignableFrom(long.class)
                        || returnType.isAssignableFrom(float.class)
                        || returnType.isAssignableFrom(double.class)
                        || returnType.isAssignableFrom(short.class)
                        || returnType.isAssignableFrom(char.class)) {
                    return 0;
                } else if (returnType.isAssignableFrom(boolean.class)) {
                    return false;
                }
                return null;
            }
            return holder.get(getter.value());
        }
        Setter setter = method.getAnnotation(Setter.class);
        if (setter != null) {
            if (args == null || args.length != 1) {
                throw new IllegalStateException("Setter method must have only one arguments.");
            }
            stateManager.getStateHolder(stateClass, false).set(setter.value(), (Serializable) args[0]);
        }
        return null;
    }
}
