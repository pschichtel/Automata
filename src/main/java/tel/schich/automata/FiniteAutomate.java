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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.ExpectedTransition;
import tel.schich.automata.transition.SpontaneousTransition;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.transition.WildcardTransition;
import tel.schich.automata.util.UnorderedPair;

import static java.util.Collections.disjoint;
import static java.util.Collections.unmodifiableSet;
import static tel.schich.automata.util.Util.asSet;
import static tel.schich.automata.util.Util.fixPointIterate;

public abstract class FiniteAutomate<T extends Transition>
{
    private final Set<State> states;
    private final Set<T> transitions;
    private final Set<State> acceptingStates;
    private final State start;

    private final Set<State> reachableStates;

    protected FiniteAutomate(Set<State> states, Set<T> transitions, State start, Set<State> acceptingStates)
    {

        states = new HashSet<>(states);
        states.addAll(acceptingStates);
        states.add(start);

        this.states = unmodifiableSet(states);
        this.transitions = unmodifiableSet(transitions);
        this.acceptingStates = unmodifiableSet(acceptingStates);
        this.start = start;
        this.reachableStates = unmodifiableSet(findReachableStates());
    }

    private Set<State> findReachableStates()
    {
        return fixPointIterate(asSet(getStartState()), in -> {
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
        Set<Character> chars = new HashSet<Character>();

        for (Transition transition : transitions)
        {
            if (transition instanceof CharacterTransition)
            {
                chars.add(((CharacterTransition)transition).getWith());
            }
        }

        return chars;
    }

    @SuppressWarnings("unchecked")
    public NFA and(FiniteAutomate<? extends Transition> other)
    {
        final Set<State> states = mergeStates(this, other);
        final Set<Transition> transitions = mergeTransitions(this, other);

        for (State state : this.getAcceptingStates())
        {
            transitions.add(new SpontaneousTransition(state, other.getStartState()));
        }

        return new NFA(states, transitions, this.getStartState(), other.getAcceptingStates());
    }

    @SuppressWarnings("unchecked")
    public NFA or(FiniteAutomate<? extends Transition> other)
    {
        final Set<State> states = mergeStates(this, other);
        final Set<Transition> transitions = mergeTransitions(this, other);

        final State start = new State();
        final State accept = new State();

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

        return new NFA(states, transitions, start, asSet(accept));
    }

    public NFA kleenePlus()
    {
        final Set<State> states = new HashSet<State>(getStates());
        final Set<Transition> transitions = new HashSet<Transition>(getTransitions());

        final State start = new State();
        final State accept = new State();

        transitions.add(new SpontaneousTransition(start, getStartState()));
        for (State state : getAcceptingStates())
        {
            transitions.add(new SpontaneousTransition(state, getStartState()));
            transitions.add(new SpontaneousTransition(state, accept));
        }

        return new NFA(states, transitions, start, asSet(accept));
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
        NFA automate = this.toNFA();
        for (int i = 1; i < n; ++i)
        {
            automate = automate.and(this);
        }
        return automate;
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
        return repeat(min - 1).and(this.kleenePlus());
    }

    public NFA repeatMinMax(int min, int max)
    {
        if (max < min)
        {
            throw new IllegalArgumentException("max must be >= min");
        }

        NFA automate = repeat(min);
        if (min == max)
        {
            return automate;
        }

        NFA maybe = this.or(NFA.EPSILON);
        for (int i = min; i < max; ++i)
        {
            automate = automate.and(maybe);
        }

        return automate;
    }

    public boolean isAccepting(State s)
    {
        return s != ErrorState.ERROR && getAcceptingStates().contains(s);
    }

    public Set<State> getReachableStates()
    {
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
        final Set<ExpectedTransition> transitions = new CopyOnWriteArraySet<ExpectedTransition>(self.getTransitions());
        State start = self.getStartState();
        final Set<State> accepting = new HashSet<>(self.getAcceptingStates());

        Set<UnorderedPair<State, State>> statePairs = new HashSet<>();

        for (State p : states)
        {
            for (State q : states)
            {
                // number of iterations can be halved by removing iterated P's from Q in P >< Q
                if (p != q)
                {
                    statePairs.add(new UnorderedPair<State, State>(p, q));
                }
            }
        }

        Set<UnorderedPair<State, State>> separableStates = new HashSet<UnorderedPair<State, State>>();
        for (UnorderedPair<State, State> p : statePairs)
        {
            // separable if either left or right is accepting
            if (self.isAccepting(p.getLeft()) != self.isAccepting(p.getRight()))
            {
                separableStates.add(p);
            }
        }

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
                    if (separableStates.contains(new UnorderedPair<State, State>(p, q)) && !separableStates.contains(
                        pair))
                    {
                        separableStates.add(pair);
                        changed = true;
                    }
                }

                // check for wildcard transition
                final State p = l.transition(self);
                final State q = r.transition(self);
                if (p == ErrorState.ERROR || q == ErrorState.ERROR)
                {
                    continue;
                }
                if (separableStates.contains(new UnorderedPair<State, State>(p, q)) && !separableStates.contains(pair))
                {
                    separableStates.add(pair);
                    changed = true;
                }
            }
        }
        while (changed);

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

            for (ExpectedTransition t : transitions)
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
                        transitions.add(new CharacterTransition(origin, ((CharacterTransition)t).getWith(),
                                                                destination));
                    }
                    else if (t instanceof WildcardTransition)
                    {
                        transitions.add(new WildcardTransition(origin, destination));
                    }
                }
            }
        }

        return new DFA(states, new HashSet<>(transitions), start, accepting);
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

    public boolean isEquivalentTo(FiniteAutomate<? extends Transition> o)
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
        if (!(o instanceof FiniteAutomate))
        {
            return false;
        }

        final FiniteAutomate<?> that = (FiniteAutomate<?>)o;

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

    protected static Set<State> mergeStates(FiniteAutomate<? extends Transition>... automates)
    {
        Set<State> states = new HashSet<State>();
        for (FiniteAutomate<? extends Transition> automate : automates)
        {
            states.addAll(automate.getStates());
        }
        return states;
    }

    protected static Set<Transition> mergeTransitions(FiniteAutomate<? extends Transition>... automates)
    {
        Set<Transition> transitions = new HashSet<Transition>();
        for (FiniteAutomate<? extends Transition> automate : automates)
        {
            transitions.addAll(automate.getTransitions());
        }
        return transitions;
    }

    public static <T extends Transition> Map<State, Set<T>> groupByState(Set<T> transitions)
    {
        Map<State, Set<T>> stateTransitions = new HashMap<State, Set<T>>();
        for (T transition : transitions)
        {
            Set<T> t = stateTransitions.get(transition.getOrigin());
            if (t == null)
            {
                t = new HashSet<T>();
                stateTransitions.put(transition.getOrigin(), t);
            }
            t.add(transition);
        }
        return stateTransitions;
    }
}
