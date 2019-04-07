/*
 * The MIT License
 * Copyright © 2014 Phillip Schichtel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tel.schich.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.PlannedTransition;
import tel.schich.automata.transition.SpontaneousTransition;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.transition.WildcardTransition;
import tel.schich.automata.util.OrderedPair;
import tel.schich.automata.util.UnorderedPair;

import static java.util.Collections.disjoint;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static tel.schich.automata.util.OrderedPair.pair;
import static tel.schich.automata.util.UnorderedPair.unorderedPair;
import static tel.schich.automata.util.Util.fixPointIterate;
import static tel.schich.automata.util.Util.unmodifiableCopy;

public abstract class FiniteAutomaton<T extends Transition>
{
    private final Set<State> states;
    private final Set<T> transitions;
    private final Set<State> acceptingStates;
    private final State start;

    private volatile Set<State> reachableStates;

    protected FiniteAutomaton(Set<State> states, Set<T> transitions, State start, Set<State> acceptingStates)
    {
        this.states = unmodifiableCopy(states);
        this.transitions = unmodifiableCopy(transitions);
        this.start = start;
        this.acceptingStates = unmodifiableCopy(acceptingStates);
    }

    private Set<State> findReachableStates()
    {
        return fixPointIterate(singleton(getStartState()), in -> {
            Set<State> out = new HashSet<>();

            for (Transition t : getTransitions())
            {
                if (t.getOrigin() == in)
                {
                    out.add(t.getDestination());
                }
            }

            return out;
        });
    }

    public Set<State> getStates()
    {
        return this.states;
    }

    public Set<T> getTransitions()
    {
        return this.transitions;
    }

    public Set<State> getAcceptingStates()
    {
        return this.acceptingStates;
    }

    public State getStartState()
    {
        return this.start;
    }

    public Set<Character> getExplicitAlphabet()
    {
        Set<Character> chars = new HashSet<>();

        for (Transition transition : transitions)
        {
            if (transition instanceof CharacterTransition)
            {
                chars.add(((CharacterTransition)transition).getWith());
            }
        }

        return chars;
    }

    public NFA concat(FiniteAutomaton<? extends Transition> other)
    {
        final Set<State> states = mergeStates(this, other);
        final Set<Transition> transitions = mergeTransitions(this, other);

        for (State state : this.getAcceptingStates())
        {
            transitions.add(new SpontaneousTransition(state, other.getStartState()));
        }

        return new NFA(states, transitions, this.getStartState(), other.getAcceptingStates());
    }

    public NFA or(FiniteAutomaton<? extends Transition> other)
    {
        final Set<State> states = mergeStates(this, other);
        final Set<Transition> transitions = mergeTransitions(this, other);

        final State start = new State();
        final State accept = new State();

        states.add(start);
        states.add(accept);

        transitions.add(new SpontaneousTransition(start, this.getStartState()));
        transitions.add(new SpontaneousTransition(start, other.getStartState()));

        for (State state : this.getAcceptingStates())
        {
            transitions.add(new SpontaneousTransition(state, accept));
        }

        for (State state : other.getAcceptingStates())
        {
            transitions.add(new SpontaneousTransition(state, accept));
        }

        return new NFA(states, transitions, start, singleton(accept));
    }

    public NFA kleenePlus()
    {
        final Set<State> states = new HashSet<>(getStates());
        final Set<Transition> transitions = new HashSet<>(getTransitions());

        final State start = new State();
        final State accept = new State();
        states.add(start);
        states.add(accept);

        transitions.add(new SpontaneousTransition(start, getStartState()));
        for (State state : getAcceptingStates())
        {
            transitions.add(new SpontaneousTransition(state, getStartState()));
            transitions.add(new SpontaneousTransition(state, accept));
        }

        return new NFA(states, transitions, start, singleton(accept));
    }

    public NFA kleeneStar()
    {
        final NFA base = kleenePlus();
        final Set<Transition> transitions = new HashSet<>(base.getTransitions());
        for (final State state : base.getAcceptingStates())
        {
            transitions.add(new SpontaneousTransition(base.getStartState(), state));
        }

        return new NFA(base.getStates(), transitions, base.getStartState(), base.getAcceptingStates());
    }

    public NFA repeat(int n)
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("Can't repeat negative amount!");
        }
        if (n == 0)
        {
            return NFA.EPSILON;
        }
        NFA automaton = this.toNFA();
        for (int i = 1; i < n; ++i)
        {
            automaton = automaton.concat(this);
        }
        return automaton;
    }

    public NFA repeatMin(int min)
    {
        if (min == 0)
        {
            return kleeneStar();
        }
        if (min == 1)
        {
            return kleenePlus();
        }
        return repeat(min - 1).concat(this.kleenePlus());
    }

    public NFA repeatMinMax(int min, int max)
    {
        if (max < min)
        {
            throw new IllegalArgumentException("max must be >= min");
        }

        NFA automaton = repeat(min);
        if (min == max)
        {
            return automaton;
        }

        NFA maybe = this.or(NFA.EPSILON);
        for (int i = min; i < max; ++i)
        {
            automaton = automaton.concat(maybe);
        }

        return automaton;
    }

    public boolean isAccepting(State s)
    {
        return s != ErrorState.ERROR && getAcceptingStates().contains(s);
    }

    public Set<State> getReachableStates()
    {
        if (this.reachableStates == null) {
            synchronized (this) {
                if (this.reachableStates == null) {
                    // no copy, trusted source
                    this.reachableStates = unmodifiableSet(findReachableStates());
                }
            }
        }
        return this.reachableStates;
    }

    public DFA minimize()
    {
        if (isEmpty())
        {
            return DFA.EMPTY;
        }

        DFA self = toDFA();
        final Set<State> states = new HashSet<>(self.getReachableStates());
        final Set<PlannedTransition> transitions = new CopyOnWriteArraySet<>();
        State start = self.getStartState();
        final Set<State> accepting = new HashSet<>();

        for (PlannedTransition transition : self.getTransitions()) {
            if (states.contains(transition.getOrigin()) && states.contains(transition.getDestination())) {
                transitions.add(transition);
            }
        }

        for (State acceptingState : self.getAcceptingStates()) {
            if (states.contains(acceptingState)) {
                accepting.add(acceptingState);
            }
        }

        final Set<UnorderedPair<State, State>> statePairs = new HashSet<>();
        for (State p : states)
        {
            for (State q : states)
            {
                if (p != q)
                {
                    statePairs.add(unorderedPair(p, q));
                }
            }
        }

        // calculate directly separable states as the initial state of the following fix-point iteration
        final Set<UnorderedPair<State, State>> separableStates = new HashSet<>();
        for (UnorderedPair<State, State> p : statePairs)
        {
            // separable if either left or right is accepting
            if (self.isAccepting(p.getLeft()) != self.isAccepting(p.getRight()))
            {
                separableStates.add(p);
            }
        }

        // iteratively calculate all separable states
        final Set<Character> alphabet = getExplicitAlphabet();
        boolean changed;
        do
        {
            changed = false;
            for (UnorderedPair<State, State> pair : statePairs)
            {
                final State l = pair.getLeft();
                final State r = pair.getRight();

                // check for explicit alphabet
                for (Character c : alphabet)
                {
                    final State p = l.transition(self, c);
                    final State q = r.transition(self, c);
                    if (p == ErrorState.ERROR || q == ErrorState.ERROR)
                    {
                        continue;
                    }
                    if (separableStates.contains(unorderedPair(p, q)) && !separableStates.contains(pair))
                    {
                        separableStates.add(pair);
                        changed = true;
                        break;
                    }
                }

                if (!changed)
                {
                    // check for wildcard transition
                    final State p = l.transition(self);
                    final State q = r.transition(self);
                    if (p == ErrorState.ERROR || q == ErrorState.ERROR) {
                        continue;
                    }
                    if (separableStates.contains(unorderedPair(p, q)) && !separableStates.contains(pair)) {
                        separableStates.add(pair);
                        changed = true;
                    }
                }
            }
        }
        while (changed);

        // drop the separable pairs in order to leave just the "useless" pairs
        statePairs.removeAll(separableStates);

        for (UnorderedPair<State, State> pair : statePairs)
        {
            final State p = pair.getLeft();
            final State q = pair.getRight();

            states.remove(q);
            accepting.remove(q);
            if (start == q)
            {
                start = p;
            }

            // TODO refactor this to not mutate during the iteration
            for (PlannedTransition t : transitions)
            {
                State origin = t.getOrigin();
                State destination = t.getDestination();
                if (origin.equals(q))
                {
                    origin = p;
                }
                if (destination.equals(q))
                {
                    destination = p;
                }

                if (origin != t.getOrigin() || destination != t.getDestination())
                {
                    transitions.remove(t);
                    if (t instanceof CharacterTransition)
                    {
                        transitions.add(new CharacterTransition(origin, ((CharacterTransition)t).getWith(), destination));
                    }
                    else if (t instanceof WildcardTransition)
                    {
                        transitions.add(new WildcardTransition(origin, destination));
                    }
                }
            }
        }

        Set<OrderedPair<State, State>> wildcards = new HashSet<>();
        Set<PlannedTransition> remove = new HashSet<>();
        for (PlannedTransition transition : transitions) {
            if (transition instanceof WildcardTransition) {
                wildcards.add(pair(transition.getOrigin(), transition.getDestination()));
            }
        }
        for (PlannedTransition t : transitions) {
            if (!(t instanceof WildcardTransition)) {
                if (wildcards.contains(pair(t.getOrigin(), t.getDestination()))) {
                    remove.add(t);
                }
            }
        }
        transitions.removeAll(remove);

        return new DFA(states, transitions, start, accepting);
    }

    public DFA complement()
    {
        final DFA complete = toDFA().complete();
        final Set<State> accepting = new HashSet<>(complete.getStates());
        accepting.removeAll(complete.getAcceptingStates());

        return new DFA(complete.getStates(), complete.getTransitions(), complete.getStartState(), accepting);
    }

    public abstract DFA toDFA();

    public abstract NFA toNFA();

    public boolean isEmpty()
    {
        return disjoint(getReachableStates(), getAcceptingStates());
    }

    public boolean isEquivalentTo(FiniteAutomaton<? extends Transition> o)
    {
        final DFA self = toDFA();
        final DFA other = o.toDFA();

        return self.difference(other).isEmpty() && other.difference(self).isEmpty();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FiniteAutomaton))
        {
            return false;
        }

        final FiniteAutomaton<?> that = (FiniteAutomaton<?>)o;

        if (!states.equals(that.states))
        {
            return false;
        }
        if (!transitions.equals(that.transitions))
        {
            return false;
        }
        if (!acceptingStates.equals(that.acceptingStates))
        {
            return false;
        }
        return start.equals(that.start);
    }

    @Override
    public int hashCode()
    {
        int result = states.hashCode();
        result = 31 * result + transitions.hashCode();
        result = 31 * result + acceptingStates.hashCode();
        result = 31 * result + start.hashCode();
        return result;
    }

    private static Set<State> mergeStates(FiniteAutomaton<? extends Transition> left, FiniteAutomaton<? extends Transition> right)
    {
        Set<State> states = new HashSet<>(left.getStates());
        states.addAll(right.getStates());
        return states;
    }

    private static Set<Transition> mergeTransitions(FiniteAutomaton<? extends Transition> left, FiniteAutomaton<? extends Transition> right)
    {
        Set<Transition> transitions = new HashSet<>(left.getTransitions());
        transitions.addAll(right.getTransitions());
        return transitions;
    }

    public static <T extends Transition> Map<State, Set<T>> groupByState(Set<T> transitions)
    {
        Map<State, Set<T>> stateTransitions = new HashMap<>();
        for (T transition : transitions)
        {
            Set<T> t = stateTransitions.computeIfAbsent(transition.getOrigin(), k -> new HashSet<>());
            t.add(transition);
        }
        return stateTransitions;
    }
}
