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

import de.cubeisland.engine.parser.rule.token.automate.transition.CharacterTransition;
import de.cubeisland.engine.parser.rule.token.automate.transition.ExpectedTransition;
import de.cubeisland.engine.parser.rule.token.automate.transition.Transition;
import de.cubeisland.engine.parser.rule.token.automate.transition.WildcardTransition;
import de.cubeisland.engine.parser.util.UnorderedPair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static de.cubeisland.engine.parser.Util.asSet;
import static de.cubeisland.engine.parser.rule.token.automate.ErrorState.ERROR;

public class DFA extends FiniteAutomate<ExpectedTransition>
{
    public static final DFA EMPTY;

    static
    {
        State a = new State();
        State b = new State();
        EMPTY = new DFA(asSet(a, b), Collections.<ExpectedTransition>emptySet(), a, asSet(b));
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

    public State getBy(State s, char c)
    {
        Transition t = getTransitionFor(s, c);
        if (t == null)
        {
            return ERROR;
        }
        return t.getDestination();
    }

    public State getByWildcard(State s)
    {
        final TransitionMap transitionMap = this.transitionLookup.get(s);
        if (transitionMap == null)
        {
            return ERROR;
        }
        Transition t = transitionMap.getWildcard();
        if (t == null)
        {
            return ERROR;
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

    public DFA complete()
    {
        final Set<State> states = new HashSet<State>(getStates());
        final Set<ExpectedTransition> transitions = new HashSet<ExpectedTransition>(getTransitions());
        final State start = getStartState();
        final Set<State> accepting = getAcceptingStates();


        final State catchAll = new State();
        states.add(catchAll);

        Set<State> stateWithWildcard = new HashSet<State>();
        for (final ExpectedTransition transition : transitions)
        {
            if (transition instanceof WildcardTransition)
            {
                stateWithWildcard.add(transition.getOrigin());
            }
        }

        for (State state : states)
        {
            if (!stateWithWildcard.contains(state))
            {
                transitions.add(new WildcardTransition(state, catchAll));
            }
        }

        return new DFA(states, transitions, start, accepting);
    }
}
