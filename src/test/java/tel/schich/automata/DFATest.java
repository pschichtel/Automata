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

import tel.schich.automata.match.PatternParser;
import tel.schich.automata.match.RegexParser;
import tel.schich.automata.eval.Evaluator;
import tel.schich.automata.eval.StateMachineEvaluator;
import tel.schich.automata.match.Matcher;
import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.PlannedTransition;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.transition.WildcardTransition;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static tel.schich.automata.util.Util.asSet;
import static tel.schich.automata.util.TestPrinting.*;

public class DFATest
{
    @Test
    public void testGetBy()
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
    public void testMinimize()
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
        Set<PlannedTransition> transitions = asSet(
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

        DFA stroeti51 = new DFA(states, transitions, q0, asSet(q3, q4));

        printAutomoton("not minimized", stroeti51);

        DFA minimized = stroeti51.minimize();
        printAutomoton("minimized", minimized);

        assertThat("Start states not equal", minimized.getStartState(), is(stroeti51.getStartState()));
        assertThat("Unexpected number of states", minimized.getStates().size(), is(3));
    }

    @Test
    public void testComplete()
    {
        final State s1 = new State();
        final State s2 = new State();
        final char acceptedChar = 'a';

        final PlannedTransition t = new CharacterTransition(s1, acceptedChar, s2);

        final DFA a = new DFA(asSet(s1, s2), asSet(t), s1, asSet(s2));
        assertFalse("a should not be complete", a.isComplete());

        final DFA completeA = a.complete();
        assertTrue("completeA should be complete", completeA.isComplete());

        final DFA completeCompleteA = completeA.complete();
        assertTrue("completeCompleteA should still be complete", completeCompleteA.isComplete());

        assertSame("repeated complete() calls should not change anything", completeA, completeCompleteA);
    }

    @Test
    public void testComplement()
    {
        final State s1 = new State();
        final State s2 = new State();
        final char acceptedChar = 'a';
        final char rejectedChar = 'r';

        final PlannedTransition t = new CharacterTransition(s1, acceptedChar, s2);

        final DFA a = new DFA(asSet(s1, s2), asSet(t), s1, asSet(s2));
        final DFA aComplement = a.complement();
        final DFA aAgain = aComplement.complement();

        printAutomoton("base a", a);
        printAutomoton("complement a", aComplement);
        printAutomoton("complement complement a", aAgain);

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
    public void testMinimizeWithWildcard()
    {
        State s0 = new State();
        State s1 = new State();
        State s2 = new State();
        final Set<State> states = asSet(s0, s1, s2);

        Set<PlannedTransition> transitions = new HashSet<>();
        transitions.add(new WildcardTransition(s0, s1));
        transitions.add(new WildcardTransition(s1, s2));
        transitions.add(new WildcardTransition(s2, s0));

        DFA a = new DFA(states, transitions, s0, states);
        DFA aMin = a.minimize();

        printAutomoton("a unminimized", a);
        printAutomoton("a minimized", aMin);
    }

    @Test
    public void testMinimizeSimple()
    {
        State s0 = new State();
        State s1 = new State();
        State s2 = new State();
        State s3 = new State();
        final Set<State> states = asSet(s0, s1, s2, s3);

        Set<PlannedTransition> transitions = new HashSet<>();
        transitions.add(new CharacterTransition(s0, 'a', s1));
        transitions.add(new CharacterTransition(s1, 'a', s2));
        transitions.add(new CharacterTransition(s2, 'a', s3));
        transitions.add(new WildcardTransition(s3, s0));

        DFA a = new DFA(states, transitions, s0, states);
        DFA aMin = a.minimize();

        printAutomoton("a unminimized", a);
        printAutomoton("a minimized", aMin);
    }

    @Test
    public void testCombine()
    {
        State _1 = new NamedState("1");
        State _2 = new NamedState("2");
        State A = new NamedState("A");
        State B = new NamedState("B");
        State C = new NamedState("C");

        DFA a1 = new DFA(asSet(_1, _2), asSet(new CharacterTransition(_1, 'a', _2)), _1, asSet(_2));
        DFA a2 = new DFA(asSet(A, B, C), asSet(
            new CharacterTransition(A, 'b', B),
            new CharacterTransition(B, 'b', C)
        ), A, asSet(C));

        printAutomoton("a1", a1);
        printAutomoton("a2", a2);


        final DFA union = a1.union(a2);
        printAutomoton("a1 union a2", union);
        automatonToDot("a1 union a2", union);
        assertTrue("union: a", matchAgainstString(union, "a"));
        assertTrue("union: bb", matchAgainstString(union, "bb"));
        assertFalse("union: b", matchAgainstString(union, "b"));
        assertFalse("union: ba", matchAgainstString(union, "ba"));
        assertFalse("union: abb", matchAgainstString(union, "abb"));
        assertFalse("union: bba", matchAgainstString(union, "bba"));
        assertFalse("union: bab", matchAgainstString(union, "bab"));
        assertFalse("union: baa", matchAgainstString(union, "baa"));
        assertFalse("union: aab", matchAgainstString(union, "aab"));
        assertFalse("union: aba", matchAgainstString(union, "aba"));
        assertFalse("union: aa", matchAgainstString(union, "aa"));
        assertFalse("union: ab", matchAgainstString(union, "ab"));


        final DFA intersection = a1.intersectWith(a2);
        printAutomoton("a1 intersected by a2", intersection);
        assertFalse("intersection: abb", matchAgainstString(intersection, "abb"));
        assertFalse("intersection: bba", matchAgainstString(intersection, "bba"));
        assertFalse("intersection: bab", matchAgainstString(intersection, "bab"));
        assertFalse("intersection: bb", matchAgainstString(intersection, "bb"));
        assertFalse("intersection: b", matchAgainstString(intersection, "b"));
        assertFalse("intersection: ba", matchAgainstString(intersection, "ba"));
        assertFalse("intersection: baa", matchAgainstString(intersection, "baa"));
        assertFalse("intersection: aab", matchAgainstString(intersection, "aab"));
        assertFalse("intersection: aba", matchAgainstString(intersection, "aba"));
        assertFalse("intersection: aa", matchAgainstString(intersection, "aa"));
        assertFalse("intersection: ab", matchAgainstString(intersection, "ab"));
        assertFalse("intersection: a", matchAgainstString(intersection, "a"));


        final DFA difference = a1.without(a2);
        printAutomoton("a1 without a2", difference);
        assertTrue("difference: a", matchAgainstString(difference, "a"));
        assertFalse("difference: ab", matchAgainstString(difference, "ab"));
        assertFalse("difference: ba", matchAgainstString(difference, "ba"));
        assertFalse("difference: bb", matchAgainstString(difference, "bb"));
        assertFalse("difference: b", matchAgainstString(difference, "b"));
        assertFalse("difference: abb", matchAgainstString(difference, "abb"));
        assertFalse("difference: bba", matchAgainstString(difference, "bba"));
        assertFalse("difference: bab", matchAgainstString(difference, "bab"));
        assertFalse("difference: baa", matchAgainstString(difference, "baa"));
        assertFalse("difference: aab", matchAgainstString(difference, "aab"));
        assertFalse("difference: aba", matchAgainstString(difference, "aba"));
        assertFalse("difference: aa", matchAgainstString(difference, "aa"));
    }

    @Test
    public void testCombineWithWildcard()
    {
        State _1 = new NamedState("1");
        State _2 = new NamedState("2");
        State A = new NamedState("A");
        State B = new NamedState("B");
        State C = new NamedState("C");

        DFA a1 = new DFA(asSet(_1, _2), asSet(new CharacterTransition(_1, 'a', _2)), _1, asSet(_2));
        DFA a2 = new DFA(asSet(A, B, C), asSet(
            new WildcardTransition(A, B),
            new WildcardTransition(B, C)
        ), A, asSet(C));

        printAutomoton("a1", a1);
        printAutomoton("a2", a2);

        final DFA union = a1.union(a2);
        printAutomoton("a1 union a2", union);
        assertFalse("union: b", matchAgainstString(union, "b"));
        assertTrue("union: ba", matchAgainstString(union, "ba"));
        assertTrue("union: bb", matchAgainstString(union, "bb"));
        assertFalse("union: abb", matchAgainstString(union, "abb"));
        assertFalse("union: bba", matchAgainstString(union, "bba"));
        assertFalse("union: bab", matchAgainstString(union, "bab"));
        assertFalse("union: baa", matchAgainstString(union, "baa"));
        assertFalse("union: aab", matchAgainstString(union, "aab"));
        assertFalse("union: aba", matchAgainstString(union, "aba"));
        assertTrue("union: aa", matchAgainstString(union, "aa"));
        assertTrue("union: ab", matchAgainstString(union, "ab"));
        assertTrue("union: a", matchAgainstString(union, "a"));


        final DFA intersection = a1.intersectWith(a2);
        automatonToDot("a1 intersected by a2", intersection);
        assertTrue(intersection.isEmpty());
        assertFalse("intersection: b", matchAgainstString(intersection, "b"));
        assertFalse("intersection: ba", matchAgainstString(intersection, "ba"));
        assertFalse("intersection: bb", matchAgainstString(intersection, "bb"));
        assertFalse("intersection: abb", matchAgainstString(intersection, "abb"));
        assertFalse("intersection: bba", matchAgainstString(intersection, "bba"));
        assertFalse("intersection: bab", matchAgainstString(intersection, "bab"));
        assertFalse("intersection: baa", matchAgainstString(intersection, "baa"));
        assertFalse("intersection: aab", matchAgainstString(intersection, "aab"));
        assertFalse("intersection: aba", matchAgainstString(intersection, "aba"));
        assertFalse("intersection: aa", matchAgainstString(intersection, "aa"));
        assertFalse("intersection: ab", matchAgainstString(intersection, "ab"));
        assertFalse("intersection: a", matchAgainstString(intersection, "a"));


        final DFA difference = a1.without(a2);
        printAutomoton("a1 without a2", difference);
        assertTrue("difference: a", matchAgainstString(difference, "a"));
        assertFalse("difference: b", matchAgainstString(difference, "b"));
        assertFalse("difference: ba", matchAgainstString(difference, "ba"));
        assertFalse("difference: bb", matchAgainstString(difference, "bb"));
        assertFalse("difference: abb", matchAgainstString(difference, "abb"));
        assertFalse("difference: bba", matchAgainstString(difference, "bba"));
        assertFalse("difference: bab", matchAgainstString(difference, "bab"));
        assertFalse("difference: baa", matchAgainstString(difference, "baa"));
        assertFalse("difference: aab", matchAgainstString(difference, "aab"));
        assertFalse("difference: aba", matchAgainstString(difference, "aba"));
        assertFalse("difference: aa", matchAgainstString(difference, "aa"));
        assertFalse("difference: ab", matchAgainstString(difference, "ab"));
    }

    private static boolean matchAgainstString(FiniteAutomaton<? extends Transition> automaton, String str)
    {
        StateMachineEvaluator evaluator = Evaluator.eval(automaton);
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
    public void testEquivalence()
    {
        DFA a = RegexParser.toDFA("a");
        DFA b = RegexParser.toDFA("a");
        DFA c = RegexParser.toDFA("c");

        assertTrue("a and b should be equivalent", a.isEquivalentTo(b));
        assertFalse("a and c should not be equivalent", a.isEquivalentTo(c));

        DFA complexA = RegexParser.toDFA(".*a");
        DFA complexB = RegexParser.toDFA("a");
        DFA complexC = RegexParser.toDFA("c");


        assertFalse("complexA and complexB should be equivalent", complexA.isEquivalentTo(complexB));
        assertFalse("complexA and complexC should not be equivalent", complexA.isEquivalentTo(complexC));
    }

    @Test
    public void testIntersection()
    {
        DFA first = PatternParser.toDFA("a*b*c*d*");
        DFA seconds = PatternParser.toDFA("d*c*b*a*");

        automatonToDot("first", first);
        automatonToDot("second", seconds);

        DFA intersection = first.intersectWith(seconds);
        automatonToDot("intersection", intersection.minimize());

        assertFalse(intersection.isEmpty());
    }
}
