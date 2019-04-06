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
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;

import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.PlannedTransition;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.transition.WildcardTransition;
import tel.schich.automata.util.OrderedPair;
import tel.schich.automata.util.Pair;

import static java.util.Collections.emptySet;
import static tel.schich.automata.util.Util.asSet;

public class DFA extends FiniteAutomaton<PlannedTransition>
{
    public static final DFA EMPTY;

    static
    {
        State a = new State();
        EMPTY = new DFA(asSet(a), asSet(new WildcardTransition(a, a)), a, emptySet());
    }

    private final Map<State, TransitionMap> transitionLookup;

    public DFA(Set<State> states, Set<PlannedTransition> transitions, State start, Set<State> acceptingStates)
    {
        super(states, transitions, start, acceptingStates);
        this.transitionLookup = calculateTransitionLookup(transitions);
    }

    private static Map<State, TransitionMap> calculateTransitionLookup(Set<PlannedTransition> transitions)
    {
        final Map<State, TransitionMap> transitionLookup = new HashMap<>();

        for (Map.Entry<State, Set<PlannedTransition>> entry : groupByState(transitions).entrySet())
        {
            transitionLookup.put(entry.getKey(), TransitionMap.build(entry.getValue()));
        }

        return transitionLookup;
    }

    public PlannedTransition getTransitionFor(State s, char c)
    {
        TransitionMap transitionMap = this.transitionLookup.get(s);
        if (transitionMap == null)
        {
            return null;
        }
        return transitionMap.getTransitionFor(c);
    }

    public State transition(State s, char c)
    {
        Transition t = getTransitionFor(s, c);
        if (t == null)
        {
            return ErrorState.ERROR;
        }
        return t.getDestination();
    }

    public State transitionExplicit(State s, char c)
    {
        TransitionMap transitionMap = this.transitionLookup.get(s);
        if (transitionMap == null)
        {
            return ErrorState.ERROR;
        }
        PlannedTransition t = transitionMap.getTransitionFor(c, null);
        if (t == null)
        {
            return ErrorState.ERROR;
        }
        return t.getDestination();
    }

    public State getByWildcard(State s)
    {
        final TransitionMap transitionMap = this.transitionLookup.get(s);
        if (transitionMap == null)
        {
            return ErrorState.ERROR;
        }
        Transition t = transitionMap.getWildcard();
        if (t == null)
        {
            return ErrorState.ERROR;
        }
        return t.getDestination();
    }

    @Override
    public DFA toDFA()
    {
        return this;
    }

    @Override
    public NFA toNFA()
    {
        // stupid collection API!
        Set<Transition> transitions = new HashSet<>(getTransitions());
        return new NFA(getStates(), transitions, getStartState(), getAcceptingStates());
    }

    public boolean isComplete() {

        Set<State> stateWithWildcard = new HashSet<>();
        for (final PlannedTransition transition : getTransitions())
        {
            if (transition instanceof WildcardTransition)
            {
                stateWithWildcard.add(transition.getOrigin());
            }
        }

        return stateWithWildcard.size() == getStates().size();
    }

    public DFA complete()
    {

        Set<State> stateWithWildcard = new HashSet<>();
        for (final PlannedTransition transition : getTransitions())
        {
            if (transition instanceof WildcardTransition)
            {
                stateWithWildcard.add(transition.getOrigin());
            }
        }

        if (stateWithWildcard.size() < getStates().size())
        {
            final Set<State> states = new HashSet<>(getStates());
            final Set<PlannedTransition> transitions = new HashSet<>(getTransitions());
            final State start = getStartState();
            final Set<State> accepting = getAcceptingStates();

            final State catchAll = new State();
            states.add(catchAll);

            for (State state : states)
            {
                if (!stateWithWildcard.contains(state))
                {
                    transitions.add(new WildcardTransition(state, catchAll));
                }
            }

            return new DFA(states, transitions, start, accepting);
        }

        return this;
    }


    public DFA combine(FiniteAutomaton<? extends Transition> o, BiPredicate<Boolean, Boolean> shouldAccept)
    {
        final DFA self = toDFA().complete();
        final DFA other = o.toDFA().complete();
        final Map<Pair<State, State>, State> stateMap = new HashMap<>();
        final Set<State> accepting = new HashSet<>();

        Set<Character> alphabet = new HashSet<>(self.getExplicitAlphabet());
        alphabet.addAll(other.getExplicitAlphabet());

        for (final State selfState : self.getStates())
        {
            for (final State otherState : other.getStates())
            {
                final State newState = new NamedState(selfState.getLabel() + "|" + otherState.getLabel());
                stateMap.put(new OrderedPair<>(selfState, otherState), newState);
                if (shouldAccept.test(self.isAccepting(selfState), other.isAccepting(otherState)))
                {
                    accepting.add(newState);
                }
            }
        }
        final State start = stateMap.get(new OrderedPair<>(self.getStartState(), other.getStartState()));

        Set<PlannedTransition> transitions = new HashSet<>();
        for (final Entry<Pair<State, State>, State> e : stateMap.entrySet())
        {
            final State a = e.getKey().getLeft();
            final State b = e.getKey().getRight();
            final State ab = e.getValue();

            /// check against wildcard
            State aNext = a.transition(self);
            State bNext = b.transition(other);
            transitions.add(new WildcardTransition(ab, stateMap.get(new OrderedPair<>(aNext, bNext))));

            // check against alphabet
            for (final char c : alphabet)
            {
                aNext = a.transition(self, c);
                bNext = b.transition(other, c);
                final State abNext = stateMap.get(new OrderedPair<>(aNext, bNext));

                // if there is already a wildcard between these states, another explicit transition is useless
                boolean wildcardExisting = false;
                for (final PlannedTransition transition : transitions)
                {
                    if (transition instanceof WildcardTransition)
                    {
                        WildcardTransition w = (WildcardTransition)transition;
                        if (w.getOrigin() == ab && w.getDestination() == abNext)
                        {
                            wildcardExisting = true;
                            break;
                        }
                    }
                }
                if (!wildcardExisting)
                {
                    transitions.add(new CharacterTransition(ab, c, abNext));
                }
            }
        }

        return new DFA(new HashSet<>(stateMap.values()), transitions, start, accepting);
    }

    public DFA difference(FiniteAutomaton<? extends Transition> other) {
        return this.intersectWith(other.complement());
    }

    public DFA union(FiniteAutomaton<? extends Transition> other)
    {
        return combine(other, (a, b) -> a || b);
    }

    public DFA intersectWith(FiniteAutomaton<? extends Transition> other)
    {
        return combine(other, (a, b) -> a && b);
    }

    public DFA without(FiniteAutomaton<? extends Transition> other)
    {
        return combine(other, (a, b) -> a && !b);
    }
}
