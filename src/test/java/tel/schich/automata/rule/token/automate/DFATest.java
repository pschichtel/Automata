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
package tel.schich.automata.rule.token.automate;

import tel.schich.automata.DFA;
import tel.schich.automata.FiniteAutomate;
import tel.schich.automata.NamedState;
import tel.schich.automata.State;
import tel.schich.automata.util.Util;
import tel.schich.automata.eval.Evaluator;
import tel.schich.automata.eval.StateMachineEvaluator;
import tel.schich.automata.match.Matcher;
import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.ExpectedTransition;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.transition.WildcardTransition;
import tel.schich.automata.util.PrintingUtil;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DFATest
{
    @Test
    public void testGetBy() throws Exception
    {
        String string = "Test123";
        String string2 = "super";
        DFA a = Matcher.match(string).or(Matcher.match(string2)).toDFA();

        State s = a.getStartState();
        for (char c : string.toCharArray())
        {
            System.out.println("Current state: " + s);
            s = s.transition(a, c);
        }

        assertThat("String was not matched!", a.isAccepting(s), is(true));

        s = a.getStartState();
        for (char c : string2.toCharArray())
        {
            System.out.println("Current state: " + s);
            s = s.transition(a, c);
        }

        assertThat("String2 was not matched!", a.isAccepting(s), is(true));
    }

    @Test
    public void testMinimize() throws Exception
    {
        State q0 = new NamedState("q0");
        State q1 = new NamedState("q1");
        State q2 = new NamedState("q2");
        State q3 = new NamedState("q3");
        State q4 = new NamedState("q4");
        State q5 = new NamedState("q5");
        State q6 = new NamedState("q6");
        State q7 = new NamedState("q7");

        Set<State> states = Util.asSet(q0, q1, q2, q3, q4, q5, q6, q7);
        Set<ExpectedTransition> transitions = Util.<ExpectedTransition>asSet(
                new CharacterTransition(q0, 'a', q1),
                new CharacterTransition(q0, 'b', q2),
                new CharacterTransition(q1, 'a', q3),
                new CharacterTransition(q1, 'b', q3),
                new CharacterTransition(q2, 'a', q4),
                new CharacterTransition(q2, 'b', q4),
                new CharacterTransition(q3, 'a', q1),
                new CharacterTransition(q3, 'b', q1),
                new CharacterTransition(q4, 'a', q2),
                new CharacterTransition(q4, 'b', q2),
                new CharacterTransition(q5, 'c', q6),
                new CharacterTransition(q6, 'd', q7)
        );

        DFA stroeti51 = new DFA(states, transitions, q0, Util.asSet(q3, q4));

        PrintingUtil.printAutomate("not minimized", stroeti51);

        DFA minimized = stroeti51.minimize();
        PrintingUtil.printAutomate("minimized", minimized);

        assertThat("Start states not equal", minimized.getStartState(), is(stroeti51.getStartState()));
        assertThat("Unexpected number of states", minimized.getStates().size(), is(3));
    }

    @Test
    public void testComplete() throws Exception
    {
        final State s1 = new State();
        final State s2 = new State();
        final char acceptedChar = 'a';

        final ExpectedTransition t = new CharacterTransition(s1, acceptedChar, s2);

        final DFA a = new DFA(Util.asSet(s1, s2), Util.asSet(t), s1, Util.asSet(s2));
        final DFA completeA = a.complete();
        final DFA completeCompleteA = completeA.complete();

        assertSame("repeated complete() calls should not change anything", completeA, completeCompleteA);
    }

    @Test
    public void testComplement() throws Exception
    {
        final State s1 = new State();
        final State s2 = new State();
        final char acceptedChar = 'a';
        final char rejectedChar = 'r';

        final ExpectedTransition t = new CharacterTransition(s1, acceptedChar, s2);

        final DFA a = new DFA(Util.asSet(s1, s2), Util.asSet(t), s1, Util.asSet(s2));
        final DFA aComplement = a.complement();
        final DFA aAgain = aComplement.complement();

        PrintingUtil.printAutomate("base a", a);
        PrintingUtil.printAutomate("complement a", aComplement);
        PrintingUtil.printAutomate("complement complement a", aAgain);

        assertEquals("accepted char was accepted by complement",
                a.isAccepting(a.getStartState().transition(a, acceptedChar)),
                !aComplement.isAccepting(aComplement.getStartState().transition(aComplement, acceptedChar)));
        assertEquals("rejected char was not accepted by complement",
                a.isAccepting(a.getStartState().transition(a, rejectedChar)),
                !aComplement.isAccepting(aComplement.getStartState().transition(aComplement, rejectedChar)));
        assertEquals("accepted char was accepted by complement of complement of a",
                a.isAccepting(a.getStartState().transition(a, acceptedChar)),
                aAgain.isAccepting(aAgain.getStartState().transition(aAgain, acceptedChar)));
        assertEquals("rejected char was not accepted by complement of complement of a",
                a.isAccepting(a.getStartState().transition(a, rejectedChar)),
                aAgain.isAccepting(aAgain.getStartState().transition(aAgain, rejectedChar)));
    }

    @Test
    public void testMinimizeWithWildcard() throws Exception
    {
        State s0 = new State();
        State s1 = new State();
        State s2 = new State();
        final Set<State> states = Util.asSet(s0, s1, s2);

        Set<ExpectedTransition> transitions = new HashSet<ExpectedTransition>();
        transitions.add(new WildcardTransition(s0, s1));
        transitions.add(new WildcardTransition(s1, s2));
        transitions.add(new WildcardTransition(s2, s0));

        DFA a = new DFA(states, transitions, s0, states);
        DFA aMin = a.minimize();

        PrintingUtil.printAutomate("a unminimized", a);
        PrintingUtil.printAutomate("a minimized", aMin);
    }

    @Test
    public void testMinimizeSimple() throws Exception
    {
        State s0 = new State();
        State s1 = new State();
        State s2 = new State();
        State s3 = new State();
        final Set<State> states = Util.asSet(s0, s1, s2, s3);

        Set<ExpectedTransition> transitions = new HashSet<ExpectedTransition>();
        transitions.add(new CharacterTransition(s0, 'a', s1));
        transitions.add(new CharacterTransition(s1, 'a', s2));
        transitions.add(new CharacterTransition(s2, 'a', s3));
        transitions.add(new WildcardTransition(s3, s0));

        DFA a = new DFA(states, transitions, s0, states);
        DFA aMin = a.minimize();

        PrintingUtil.printAutomate("a unminimized", a);
        PrintingUtil.printAutomate("a minimized", aMin);
    }

    @Test
    public void testCombine() throws Exception
    {
        State _1 = new NamedState("1");
        State _2 = new NamedState("2");
        State A = new NamedState("A");
        State B = new NamedState("B");
        State C = new NamedState("C");

        DFA a1 = new DFA(Util.asSet(_1, _2), Util.<ExpectedTransition>asSet(new CharacterTransition(_1, 'a', _2)), _1, Util
                .asSet(_2));
        DFA a2 = new DFA(Util.asSet(A, B, C), Util.<ExpectedTransition>asSet(
            new CharacterTransition(A, 'b', B),
            new CharacterTransition(B, 'b', C)
                                                                       ), A, Util.asSet(C));

        PrintingUtil.printAutomate("a1", a1);
        PrintingUtil.printAutomate("a2", a2);

        final DFA union = a1.union(a2);
        PrintingUtil.printAutomate("a1 union a2", union);
        assertFalse("union: b", matchAgainstString(union, "b"));
        assertTrue("union: ba", matchAgainstString(union, "ba"));
        assertTrue("union: bb", matchAgainstString(union, "bb"));
        assertTrue("union: abb", matchAgainstString(union, "abb"));
        assertTrue("union: bba", matchAgainstString(union, "bba"));
        assertTrue("union: bab", matchAgainstString(union, "bab"));
        assertFalse("union: baa", matchAgainstString(union, "baa"));
        assertFalse("union: aab", matchAgainstString(union, "aab"));
        assertFalse("union: aba", matchAgainstString(union, "aba"));
        assertFalse("union: aa", matchAgainstString(union, "aa"));
        assertTrue("union: ab", matchAgainstString(union, "ab"));
        assertTrue("union: a", matchAgainstString(union, "a"));


        final DFA intersection = a1.intersectWith(a2);
        PrintingUtil.printAutomate("a1 intersected by a2", intersection);
        assertFalse("intersection: b", matchAgainstString(intersection, "b"));
        assertFalse("intersection: ba", matchAgainstString(intersection, "ba"));
        assertFalse("intersection: bb", matchAgainstString(intersection, "bb"));
        assertTrue("intersection: abb", matchAgainstString(intersection, "abb"));
        assertTrue("intersection: bba", matchAgainstString(intersection, "bba"));
        assertTrue("intersection: bab", matchAgainstString(intersection, "bab"));
        assertFalse("intersection: baa", matchAgainstString(intersection, "baa"));
        assertFalse("intersection: aab", matchAgainstString(intersection, "aab"));
        assertFalse("intersection: aba", matchAgainstString(intersection, "aba"));
        assertFalse("intersection: aa", matchAgainstString(intersection, "aa"));
        assertFalse("intersection: ab", matchAgainstString(intersection, "ab"));
        assertFalse("intersection: a", matchAgainstString(intersection, "a"));


        final DFA difference = a1.without(a2);
        PrintingUtil.printAutomate("a1 without a2", difference);
        assertFalse("difference: b", matchAgainstString(difference, "b"));
        assertTrue("difference: ba", matchAgainstString(difference, "ba"));
        assertFalse("difference: bb", matchAgainstString(difference, "bb"));
        assertFalse("difference: abb", matchAgainstString(difference, "abb"));
        assertFalse("difference: bba", matchAgainstString(difference, "bba"));
        assertFalse("difference: bab", matchAgainstString(difference, "bab"));
        assertFalse("difference: baa", matchAgainstString(difference, "baa"));
        assertFalse("difference: aab", matchAgainstString(difference, "aab"));
        assertFalse("difference: aba", matchAgainstString(difference, "aba"));
        assertFalse("difference: aa", matchAgainstString(difference, "aa"));
        assertTrue("difference: ab", matchAgainstString(difference, "ab"));
        assertTrue("difference: a", matchAgainstString(difference, "a"));
    }

    @Test
    public void testCombineWithWildcard() throws Exception
    {
        State _1 = new NamedState("1");
        State _2 = new NamedState("2");
        State A = new NamedState("A");
        State B = new NamedState("B");
        State C = new NamedState("C");

        DFA a1 = new DFA(Util.asSet(_1, _2), Util.<ExpectedTransition>asSet(new CharacterTransition(_1, 'a', _2)), _1, Util
                .asSet(_2));
        DFA a2 = new DFA(Util.asSet(A, B, C), Util.<ExpectedTransition>asSet(
            new WildcardTransition(A, B),
            new WildcardTransition(B, C)
        ), A, Util.asSet(C));

        PrintingUtil.printAutomate("a1", a1);
        PrintingUtil.printAutomate("a2", a2);

        final DFA union = a1.union(a2);
        PrintingUtil.printAutomate("a1 union a2", union);
        assertFalse("union: b", matchAgainstString(union, "b"));
        assertTrue("union: ba", matchAgainstString(union, "ba"));
        assertTrue("union: bb", matchAgainstString(union, "bb"));
        assertTrue("union: abb", matchAgainstString(union, "abb"));
        assertTrue("union: bba", matchAgainstString(union, "bba"));
        assertTrue("union: bab", matchAgainstString(union, "bab"));
        assertTrue("union: baa", matchAgainstString(union, "baa"));
        assertTrue("union: aab", matchAgainstString(union, "aab"));
        assertTrue("union: aba", matchAgainstString(union, "aba"));
        assertTrue("union: aa", matchAgainstString(union, "aa"));
        assertTrue("union: ab", matchAgainstString(union, "ab"));
        assertTrue("union: a", matchAgainstString(union, "a"));


        final DFA intersection = a1.intersectWith(a2);
        PrintingUtil.printAutomate("a1 intersected by a2", intersection);
        assertFalse("intersection: b", matchAgainstString(intersection, "b"));
        assertFalse("intersection: ba", matchAgainstString(intersection, "ba"));
        assertFalse("intersection: bb", matchAgainstString(intersection, "bb"));
        assertTrue("intersection: abb", matchAgainstString(intersection, "abb"));
        assertTrue("intersection: bba", matchAgainstString(intersection, "bba"));
        assertTrue("intersection: bab", matchAgainstString(intersection, "bab"));
        assertTrue("intersection: baa", matchAgainstString(intersection, "baa"));
        assertTrue("intersection: aab", matchAgainstString(intersection, "aab"));
        assertTrue("intersection: aba", matchAgainstString(intersection, "aba"));
        assertFalse("intersection: aa", matchAgainstString(intersection, "aa"));
        assertFalse("intersection: ab", matchAgainstString(intersection, "ab"));
        assertFalse("intersection: a", matchAgainstString(intersection, "a"));


        final DFA difference = a1.without(a2);
        PrintingUtil.printAutomate("a1 without a2", difference);
        assertFalse("difference: b", matchAgainstString(difference, "b"));
        assertTrue("difference: ba", matchAgainstString(difference, "ba"));
        assertFalse("difference: bb", matchAgainstString(difference, "bb"));
        assertFalse("difference: abb", matchAgainstString(difference, "abb"));
        assertFalse("difference: bba", matchAgainstString(difference, "bba"));
        assertFalse("difference: bab", matchAgainstString(difference, "bab"));
        assertFalse("difference: baa", matchAgainstString(difference, "baa"));
        assertFalse("difference: aab", matchAgainstString(difference, "aab"));
        assertFalse("difference: aba", matchAgainstString(difference, "aba"));
        assertTrue("difference: aa", matchAgainstString(difference, "aa"));
        assertTrue("difference: ab", matchAgainstString(difference, "ab"));
        assertTrue("difference: a", matchAgainstString(difference, "a"));
    }

    private static boolean matchAgainstString(FiniteAutomate<? extends Transition> automate, String str)
    {
        StateMachineEvaluator evaluator = Evaluator.eval(automate);
        System.out.print(evaluator);
        for (final char c : str.toCharArray())
        {
            evaluator.transition(c);
            System.out.print(" --" + c + "--> " + evaluator);
        }
        final boolean accepted = evaluator.isCurrentAccepting();
        System.out.println("  " + (accepted ? "✓" : "X"));
        return accepted;
    }

    @Test
    public void testEquivalence() throws Exception
    {


    }
}
