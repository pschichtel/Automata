/**
 * The MIT License
 * Copyright (c) 2014 Cube Island
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
package de.cubeisland.engine.parser.rule.token.automate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import de.cubeisland.engine.parser.Util;
import de.cubeisland.engine.parser.rule.token.automate.transition.CharacterTransition;
import de.cubeisland.engine.parser.rule.token.automate.transition.ExpectedTransition;
import de.cubeisland.engine.parser.rule.token.automate.transition.SpontaneousTransition;
import de.cubeisland.engine.parser.rule.token.automate.transition.Transition;
import de.cubeisland.engine.parser.rule.token.automate.transition.WildcardTransition;
import de.cubeisland.engine.parser.util.FixPoint;
import de.cubeisland.engine.parser.util.Function;
import de.cubeisland.engine.parser.util.OrderedPair;
import de.cubeisland.engine.parser.util.Pair;

import static de.cubeisland.engine.parser.Util.asSet;

public class NFA extends FiniteAutomate<Transition>
{
    public static final NFA EMPTY;
    public static final NFA EPSILON;

    static
    {
        State a = new State();
        State b = new State();
        EPSILON = new NFA(asSet(a, b), Util.<Transition>asSet(new SpontaneousTransition(a, b)), a, asSet(b));
        EMPTY = new NFA(asSet(a, b), Collections.<Transition>emptySet(), a, asSet(b));
    }

    public Set<State> getStartStates()
    {
        return epsilonClosure(asSet(getStartState()));
    }

    private final Map<State, TransitionMultiMap> transitionLookup;

    public NFA(Set<State> states, Set<Transition> transitions, State start, Set<State> acceptingStates)
    {
        super(states, transitions, start, acceptingStates);
        this.transitionLookup = calculateTransitionLookup(transitions);
    }

    private static Map<State, TransitionMultiMap> calculateTransitionLookup(Set<Transition> transitions)
    {
        Map<State, TransitionMultiMap> transitionLookup = new HashMap<State, TransitionMultiMap>();

        for (Map.Entry<State, Set<Transition>> entry : groupByState(transitions).entrySet())
        {
            transitionLookup.put(entry.getKey(), TransitionMultiMap.build(entry.getValue()));
        }

        return transitionLookup;
    }

    public Set<SpontaneousTransition> getSpontaneousTransitionsFor(State s)
    {
        TransitionMultiMap lookup = this.transitionLookup.get(s);
        if (lookup == null)
        {
            return Collections.emptySet();
        }
        return lookup.getSpontaneousTransitions();
    }

    public Set<ExpectedTransition> getExpectedTransitionsFor(State s, char c)
    {
        TransitionMultiMap lookup = this.transitionLookup.get(s);
        if (lookup == null)
        {
            return Collections.emptySet();
        }
        return lookup.getTransitionsFor(c);
    }

    public Set<Character> getExpectedCharsFor(State s)
    {
        TransitionMultiMap lookup = this.transitionLookup.get(s);
        if (lookup == null)
        {
            return Collections.emptySet();
        }
        return lookup.getAlphabet();
    }

    public Set<State> epsilonClosure(Set<State> states)
    {
        return FixPoint.apply(states, new Function<State, Set<State>>()
        {
            @Override
            public Set<State> apply(State in)
            {
                Set<State> states = new HashSet<State>();
                for (SpontaneousTransition transition : getSpontaneousTransitionsFor(in))
                {
                    states.add(transition.getDestination());
                }
                return states;
            }
        });
    }

    private Set<Character> alphabetFor(Set<State> states)
    {
        Set<Character> chars = new HashSet<Character>();

        for (State state : states)
        {
            chars.addAll(getExpectedCharsFor(state));
        }

        return chars;
    }

    public Set<State> transition(Set<State> states, char c)
    {
        return epsilonClosure(read(states, c));
    }

    private Set<State> transitionExplicit(Set<State> states, char c)
    {
        return epsilonClosure(readExplicit(states, c));
    }

    private Set<State> transition(Set<State> states)
    {
        return epsilonClosure(read(states));
    }

    private Set<State> read(Set<State> states)
    {
        Set<State> out = new HashSet<State>();

        for (State state : states)
        {
            final TransitionMultiMap map = this.transitionLookup.get(state);
            if (map == null)
            {
                continue;
            }
            for (final WildcardTransition transition : map.getWildcards())
            {
                out.add(transition.getDestination());
            }
        }

        return out;
    }

    private Set<State> read(Set<State> states, char c)
    {
        Set<State> out = new HashSet<State>();

        for (State state : states)
        {
            for (ExpectedTransition transition : getExpectedTransitionsFor(state, c))
            {
                out.add(transition.getDestination());
            }
        }

        return out;
    }

    private Set<State> readExplicit(Set<State> states, char c)
    {
        Set<State> out = new HashSet<State>();

        for (State state : states)
        {
            final TransitionMultiMap map = this.transitionLookup.get(state);
            if (map == null)
            {
                continue;
            }
            for (final ExpectedTransition transition : map.getTransitionsFor(c,
                                                                             Collections.<ExpectedTransition>emptySet()))
            {
                out.add(transition.getDestination());
            }
        }

        return out;
    }

    public boolean isAccepting(Set<State> states)
    {
        for (final State state : states)
        {
            if (isAccepting(state))
            {
                return true;
            }
        }
        return false;
    }

    private boolean willAccept(Set<State> newState)
    {
        final Set<State> accept = getAcceptingStates();
        for (final State state : newState)
        {
            if (accept.contains(state))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public DFA toDFA()
    {
        final Set<State> states = new HashSet<State>();
        final Set<ExpectedTransition> transitions = new HashSet<ExpectedTransition>();
        final State start = getStartState();
        final Set<State> accepting = new HashSet<State>();

        Map<Set<State>, State> knownStates = new HashMap<Set<State>, State>();
        Queue<Pair<State, Set<State>>> stateQueue = new LinkedList<Pair<State, Set<State>>>();

        Set<State> initialClosure = getStartStates();
        System.out.println("1. " + start + " = ec(" + start + ") = " + initialClosure);
        stateQueue.offer(new OrderedPair<State, Set<State>>(start, initialClosure));
        knownStates.put(initialClosure, start);
        if (willAccept(initialClosure))
        {
            accepting.add(start);
        }

        int i = 1;
        while (!stateQueue.isEmpty())
        {
            final Pair<State, Set<State>> pair = stateQueue.poll();
            final State state = pair.getLeft();
            final Set<State> stateSet = pair.getRight();

            // check for wildcard edges
            Set<State> newStateSet = transition(stateSet);
            System.out.print((++i) + ". Δ(" + state + ", *) = " + newStateSet);

            if (!newStateSet.isEmpty())
            {
                State alreadyKnown = knownStates.get(newStateSet);
                if (alreadyKnown == null)
                {
                    State newState = new State();
                    states.add(newState);
                    stateQueue.offer(new OrderedPair<State, Set<State>>(newState, newStateSet));
                    knownStates.put(newStateSet, newState);
                    transitions.add(new WildcardTransition(state, newState));
                    System.out.println(" = " + newState);
                    if (willAccept(newStateSet))
                    {
                        accepting.add(newState);
                    }
                }
                else
                {
                    transitions.add(new WildcardTransition(state, alreadyKnown));
                    System.out.println(" = " + alreadyKnown);
                }
            }

            // check for explicit edges
            for (char c : alphabetFor(stateSet))
            {
                newStateSet = transitionExplicit(stateSet, c);
                System.out.print((++i) + ". Δ(" + state + ", " + c + ") = " + newStateSet);
                if (newStateSet.isEmpty())
                {
                    continue;
                }

                State alreadyKnown = knownStates.get(newStateSet);
                if (alreadyKnown == null)
                {
                    State newState = new State();
                    states.add(newState);
                    stateQueue.offer(new OrderedPair<State, Set<State>>(newState, newStateSet));
                    knownStates.put(newStateSet, newState);
                    transitions.add(new CharacterTransition(state, c, newState));
                    System.out.println(" = " + newState);
                    if (willAccept(newStateSet))
                    {
                        accepting.add(newState);
                    }
                }
                else
                {
                    transitions.add(new CharacterTransition(state, c, alreadyKnown));
                    System.out.println(" = " + alreadyKnown);
                }
            }
        }

        System.out.println("Transitions: " + transitions.size());

        return new DFA(states, transitions, start, accepting);
    }

    @Override
    public NFA toNFA()
    {
        return this;
    }
}
