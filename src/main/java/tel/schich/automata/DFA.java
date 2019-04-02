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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.ExpectedTransition;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.transition.WildcardTransition;
import tel.schich.automata.util.OrderedPair;
import tel.schich.automata.util.Pair;
import tel.schich.automata.util.Util;

public class DFA extends FiniteAutomate<ExpectedTransition>
{
    public static final DFA EMPTY;

    static
    {
        State a = new State();
        State b = new State();
        EMPTY = new DFA(Util.asSet(a, b), Collections.<ExpectedTransition>emptySet(), a, Util.asSet(b));
    }

    private final Map<State, TransitionMap> transitionLookup;

    public DFA(Set<State> states, Set<ExpectedTransition> transitions, State start, Set<State> acceptingStates)
    {
        super(states, transitions, start, acceptingStates);
        this.transitionLookup = calculateTransitionLookup(transitions);
    }

    private static Map<State, TransitionMap> calculateTransitionLookup(Set<ExpectedTransition> transitions)
    {
        final Map<State, TransitionMap> transitionLookup = new HashMap<State, TransitionMap>();

        for (Map.Entry<State, Set<ExpectedTransition>> entry : groupByState(transitions).entrySet())
        {
            transitionLookup.put(entry.getKey(), TransitionMap.build(entry.getValue()));
        }

        return transitionLookup;
    }

    public ExpectedTransition getTransitionFor(State s, char c)
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

    private State transitionExplicit(State s, char c)
    {
        TransitionMap transitionMap = this.transitionLookup.get(s);
        if (transitionMap == null)
        {
            return ErrorState.ERROR;
        }
        ExpectedTransition t = transitionMap.getTransitionFor(c, null);
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
        Set<Transition> transitions = new HashSet<Transition>();
        for (final ExpectedTransition t : getTransitions())
        {
            transitions.add(t);
        }
        return new NFA(getStates(), transitions, getStartState(), getAcceptingStates());
    }

    public boolean isComplete() {

        Set<State> stateWithWildcard = new HashSet<State>();
        for (final ExpectedTransition transition : getTransitions())
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

        Set<State> stateWithWildcard = new HashSet<State>();
        for (final ExpectedTransition transition : getTransitions())
        {
            if (transition instanceof WildcardTransition)
            {
                stateWithWildcard.add(transition.getOrigin());
            }
        }

        if (stateWithWildcard.size() < getStates().size())
        {
            final Set<State> states = new HashSet<State>(getStates());
            final Set<ExpectedTransition> transitions = new HashSet<ExpectedTransition>(getTransitions());
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

    public DFA combine(FiniteAutomate<? extends Transition> o, Combination combination)
    {
        final DFA self = toDFA();
        final DFA other = o.toDFA();
        Map<Pair<State, State>, State> stateMap = new HashMap<Pair<State, State>, State>();
        final Set<State> accepting = new HashSet<State>();

        Set<Character> alphabet = new HashSet<Character>(self.getExplicitAlphabet());
        alphabet.addAll(other.getExplicitAlphabet());

        for (final State selfState : self.getStates())
        {
            for (final State otherState : other.getStates())
            {
                final State newState = new State();
                stateMap.put(new OrderedPair<State, State>(selfState, otherState), newState);
                if (combination.isAccepting(self.isAccepting(selfState), other.isAccepting(otherState)))
                {
                    accepting.add(newState);
                }
            }
        }
        final State start = stateMap.get(new OrderedPair<State, State>(self.getStartState(), other.getStartState()));

        Set<ExpectedTransition> transitions = new HashSet<ExpectedTransition>();
        for (final Entry<Pair<State, State>, State> e : stateMap.entrySet())
        {
            final State a = e.getKey().getLeft();
            final State b = e.getKey().getRight();
            final State ab = e.getValue();

            /// check against wildcard
            State aNext = a.transition(self);
            State bNext = b.transition(other);
            if (aNext != ErrorState.ERROR || bNext != ErrorState.ERROR)
            {
                if (aNext == ErrorState.ERROR)
                {
                    aNext = a;
                }
                if (bNext == ErrorState.ERROR)
                {
                    bNext = b;
                }
                final State abNext = stateMap.get(new OrderedPair<State, State>(aNext, bNext));
                transitions.add(new WildcardTransition(ab, abNext));
            }

            // check against alphabet
            for (final char c : alphabet)
            {
                aNext = self.transitionExplicit(a, c);
                bNext = other.transitionExplicit(b, c);
                if (aNext != ErrorState.ERROR || bNext != ErrorState.ERROR)
                {
                    if (aNext == ErrorState.ERROR)
                    {
                        aNext = a;
                    }
                    if (bNext == ErrorState.ERROR)
                    {
                        bNext = b;
                    }
                    final State abNext = stateMap.get(new OrderedPair<State, State>(aNext, bNext));

                    // if there is already a wildcard between these states, another explicit transition is useless
                    boolean wildcardExisting = false;
                    for (final ExpectedTransition transition : transitions)
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
        }

        return new DFA(new HashSet<State>(stateMap.values()), transitions, start, accepting);
    }

    public DFA union(FiniteAutomate<? extends Transition> other)
    {
        return combine(other, Combination.UNION);
    }

    public DFA intersectWith(FiniteAutomate<? extends Transition> other)
    {
        return combine(other, Combination.INTERSECTION);
    }

    public DFA without(FiniteAutomate<? extends Transition> other)
    {
        return combine(other, Combination.DIFFERENCE);
    }
}
