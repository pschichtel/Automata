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
package de.cubeisland.engine.parser.rule.token.automate;

import de.cubeisland.engine.parser.Util;
import de.cubeisland.engine.parser.rule.token.automate.match.Matcher;
import de.cubeisland.engine.parser.rule.token.automate.transition.CharacterTransition;
import de.cubeisland.engine.parser.rule.token.automate.transition.SpontaneousTransition;
import de.cubeisland.engine.parser.rule.token.automate.transition.Transition;
import de.cubeisland.engine.parser.util.PrintingUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static de.cubeisland.engine.parser.Util.asSet;
import static de.cubeisland.engine.parser.util.PrintingUtil.printAutomate;

public class NFATest
{
    private NFA stroetiExample43;
    private NFA stroetiExample44;

    @Before
    public void setUp() throws Exception
    {
        State q0 = new NamedState("q0");
        State q1 = new NamedState("q1");
        State q2 = new NamedState("q2");
        State q3 = new NamedState("q3");
        State q4 = new NamedState("q4");
        State q5 = new NamedState("q5");
        State q6 = new NamedState("q6");
        State q7 = new NamedState("q7");

        Set<State> states = asSet(q0, q1, q2, q3, q4, q5, q6, q7);
        Set<Transition> transitions = asSet(
                new SpontaneousTransition(q0, q1),
                new SpontaneousTransition(q0, q2),
                new CharacterTransition(q1, 'b', q3),
                new CharacterTransition(q2, 'a', q4),
                new CharacterTransition(q3, 'a', q5),
                new CharacterTransition(q4, 'b', q6),
                new SpontaneousTransition(q5, q7),
                new SpontaneousTransition(q6, q7),
                new SpontaneousTransition(q7, q0)
        );

        this.stroetiExample44 = new NFA(states, transitions, q0, asSet(q7));

        states = asSet(q0, q1, q2, q3);
        transitions = Util.<Transition>asSet(
                new CharacterTransition(q0, 'a', q0),
                new CharacterTransition(q0, 'b', q0),
                new CharacterTransition(q0, 'b', q1),
                new CharacterTransition(q1, 'a', q2),
                new CharacterTransition(q1, 'b', q2),
                new CharacterTransition(q2, 'a', q3),
                new CharacterTransition(q2, 'b', q3)
        );

        this.stroetiExample43 = new NFA(states, transitions, q0, asSet(q3));
    }

    @Test
    public void testClosure44() throws Exception
    {
        PrintingUtil.printAutomate("Stroeti NFA example 4.4", stroetiExample44);

        for (State state : stroetiExample44.getStates())
        {
            System.out.println("ec(" + state + ") = " + stroetiExample44.epsilonClosure(asSet(state)));
        }
    }

    @Test
    public void testToDFA() throws Exception
    {
        PrintingUtil.printAutomate("toDFA", stroetiExample44.toDFA());
    }

    @Test
    public void testToDFA2() throws Exception
    {
        PrintingUtil.printAutomate("toDFA", stroetiExample43.toDFA());
    }

    @Test
    public void testWithWildcardToDFA() throws Exception
    {
        printAutomate("wildcard match", Matcher.matchWildcard());
        final NFA a = Matcher.matchWildcard().and(Matcher.matchWildcard());
        //final NFA a = Matcher.matchAll('a').and(Matcher.matchAll('a'));
        printAutomate("a", a);
        DFA dfa = a.toDFA();
        printAutomate("a as DFA", dfa);
        printAutomate("a as minimized DFA", dfa.minimize());
    }
}
