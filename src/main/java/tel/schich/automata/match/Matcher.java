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
package tel.schich.automata.match;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import tel.schich.automata.DFA;
import tel.schich.automata.State;
import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.PlannedTransition;
import tel.schich.automata.transition.WildcardTransition;

import static java.util.Collections.singleton;
import static tel.schich.automata.util.Util.asSet;

public abstract class Matcher
{
    private Matcher()
    {
    }

    public static DFA matchWildcard()
    {
        final State start = new State();
        final State accept = new State();
        final PlannedTransition t = new WildcardTransition(start, accept);
        return new DFA(asSet(start, accept), singleton(t), start, singleton(accept));
    }

    public static DFA match(String s)
    {
        return matchAll(s.toCharArray());
    }

    public static DFA matchAll(char... chars)
    {
        Set<State> states = new HashSet<>();
        Set<PlannedTransition> transitions = new HashSet<>();
        State start = new State();
        states.add(start);

        State lastState = start;

        for (char c : chars)
        {
            State state = new State();
            states.add(state);
            transitions.add(new CharacterTransition(lastState, c, state));
            lastState = state;
        }

        return new DFA(states, transitions, start, singleton(lastState));
    }

    public static DFA matchOne(char... chars)
    {
        Set<PlannedTransition> transitions = new HashSet<>();
        State start = new State();
        State end = new State();

        for (char c : chars)
        {
            transitions.add(new CharacterTransition(start, c, end));
        }

        return new DFA(asSet(start, end), transitions, start, singleton(end));
    }

    public static DFA matchJavaCompatibleRegex(String regex)
    {
        return PatternParser.toDFA(regex).minimize();
    }

    public static DFA match(Pattern pattern)
    {
        return matchJavaCompatibleRegex(pattern.toString());
    }
}
