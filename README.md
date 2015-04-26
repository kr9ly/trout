# Trout - state holder and change notifier

[![Circle CI](https://circleci.com/gh/kr9ly/trout/tree/master.svg?style=shield)](https://circleci.com/gh/kr9ly/trout/tree/master)

Trout state holder and change notifier.

# Usage

Add this to `repositories` block in your build.gradle

```
maven { url 'http://kr9ly.github.io/maven/' }
```

And Add this to `dependencies` block in your build.gradle

```
compile 'net.kr9ly:trout:0.0.1'
```

### Basic

##### 1. Create interface for state get/set.

```java
public interface SampleState {

    @Getter("foo") // annotate by net.kr9ly.trout.Getter
    String getFoo();
    
    @Setter("foo") // annotate by net.kr9ly.trout.Setter
    void setFoo(String foo);
    
    @Getter("bar")
    int getBar();
        
    @Setter("bar")
    void setBar(int bar);
}
```

##### 2. Create StateManager.

```java
StateManager manager = StateManager.newRoot();
```

##### 3. Update State.

```java
SampleState state = manager.getState(SampleState.class);
state.setFoo("sample");
state.setBar(1);

SampleState another = manager.getState(SampleState.class);
 // no state updates before commit
assert "sample".equals(another.getFoo()) == false;
assert 1 != another.getBar();
```

##### 4. Commit State.

```java
manager.commit();
```

##### 5. Get Updated State.

```java
SampleState updated = manager.getState(SampleState.class);
assert "sample".equals(updated.getFoo());
assert 1 != updated.getBar();
```

### Subscribe State Update

##### 1. Create Subscriber.

```java
public class SampleSubscriber {

    @OnUpdate("foo")
    public void onFooUpdated() {
        System.out.println("Updated foo.");
    }
    
    @OnUpdate("bar")
    public void onFooUpdated() {
        System.out.println("Updated bar.");
    }
}
```

##### 2. Subscribe State Update.

```java
manager.subscribe(SampleState.class, new SampleSubscriber());
SampleState state = manager.getState(SampleState.class);
state.setFoo("updated");
state.setBar(1);
manager.commit();
// -> "Updated foo."
// -> "Updated bar."
state.setFoo("updated");
manager.commit();
// no callback if no state change.
```

### Get Parent State

```java
StateManager root = StateManager.newRoot();
StateManager child = root.newChild();

SampleState state = root.getState(SampleState.class);
state.setFoo("parent state");

SampleState childState = child.getState(SampleState.class);

root.commit();

assert "parent state".equals(childState.getFoo());
```

```java
StateManager root = StateManager.newRoot();
StateManager child = root.newChild();

SampleState state = root.getState(SampleState.class);
state.setFoo("parent state");

child.subscribe(SampleState.class, new SampleSubscriber());

root.commit();
// -> "Updated foo."
```

# License

```
Copyright 2015 kr9ly

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```