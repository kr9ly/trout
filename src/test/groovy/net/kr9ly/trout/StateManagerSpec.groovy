package net.kr9ly.trout

import spock.lang.Specification

class StateManagerSpec extends Specification {

    def "get state after committed"() {
        setup:
        def manager = StateManager.newRoot()

        when:
        def state = manager.getState(TestState.class)
        state.foo = foo
        state.bar = bar
        manager.commit()
        def updated = manager.getState(TestState.class)

        then:
        updated.foo == foo
        updated.bar == bar

        where:
        foo | bar
        "a" | 1
    }

    def "no update before commit"() {
        setup:
        def manager = StateManager.newRoot()

        when:
        def state = manager.getState(TestState.class)
        state.foo = foo
        state.bar = bar
        manager.commit()
        def updated = manager.getState(TestState.class)
        state.foo = foo2
        state.bar = bar2

        then:
        updated.foo == foo
        updated.bar == bar

        where:
        foo | bar | foo2 | bar2
        "a" | 1   | "b"  | 2
    }

    def "publish to subscriber when state update"() {
        setup:
        def manager = StateManager.newRoot()
        def subscriber = new Subscriber()

        when:
        def state = manager.getState(TestState.class)
        state.foo = foo
        state.bar = bar
        manager.commit()
        manager.subscribe(TestState.class, subscriber)
        state.foo = foo2
        state.bar = bar2
        manager.commit()

        then:
        subscriber.isFooUpdated == resFoo
        subscriber.isBarUpdated == resBar

        where:
        foo | bar | foo2 | bar2 | resFoo | resBar
        "a" | 1   | "b"  | 2    | true   | true
        "a" | 1   | "b"  | 1    | true   | false
        "a" | 1   | "a"  | 2    | false  | true
    }

    def "only publish once on one update"() {
        setup:
        def manager = StateManager.newRoot()
        def subscriber = new Subscriber()

        when:
        def state = manager.getState(TestState.class)
        manager.subscribe(TestState.class, subscriber)
        state.foo = foo
        state.bar = bar
        manager.commit()
        subscriber.isFooUpdated = false
        subscriber.isBarUpdated = false
        manager.commit()

        then:
        !subscriber.isFooUpdated
        !subscriber.isBarUpdated

        where:
        foo | bar
        "a" | 1
    }

    def "get parent state from child state"() {
        setup:
        def root = StateManager.newRoot()
        def child = root.newChild()

        when:
        def state = root.getState(TestState.class)
        def childState = child.getState(TestState.class)
        state.foo = foo
        state.bar = bar
        root.commit()

        then:
        childState.foo == foo
        childState.bar == bar

        where:
        foo | bar
        "a" | 1
    }

    def "can't get child state from parent"() {
        setup:
        def root = StateManager.newRoot()
        def child = root.newChild()

        when:
        def state = child.getState(TestState.class)
        def parentState = root.getState(TestState.class)
        state.foo = foo
        state.bar = bar
        root.commit()

        then:
        parentState.foo != foo
        parentState.bar != bar

        where:
        foo | bar
        "a" | 1
    }

    def "publish parent state update to child subscriber"() {
        setup:
        def root = StateManager.newRoot()
        def child = root.newChild()
        def subscriber = new Subscriber()

        when:
        def state = root.getState(TestState.class)
        child.subscribe(TestState.class, subscriber)
        state.foo = foo
        state.bar = bar
        root.commit()

        then:
        subscriber.isFooUpdated
        subscriber.isBarUpdated

        where:
        foo | bar
        "a" | 1
    }

    def "no publishing to removed child subscriber"() {
        setup:
        def root = StateManager.newRoot()
        def child = root.newChild()
        def subscriber = new Subscriber()

        when:
        def state = root.getState(TestState.class)
        child.subscribe(TestState.class, subscriber)
        state.foo = foo
        state.bar = bar
        child.remove()
        root.commit()

        then:
        !subscriber.isFooUpdated
        !subscriber.isBarUpdated

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

    private class Subscriber {

        private boolean isFooUpdated = false

        private boolean isBarUpdated = false

        @OnUpdate("foo")
        public void onFooUpdated() {
            isFooUpdated = true
        }

        @OnUpdate("bar")
        public void onBarUpdated() {
            isBarUpdated = true
        }
    }
}