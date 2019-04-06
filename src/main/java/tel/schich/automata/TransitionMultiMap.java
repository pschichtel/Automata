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
import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.PlannedTransition;
import tel.schich.automata.transition.SpontaneousTransition;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.transition.WildcardTransition;

import static java.util.Collections.unmodifiableSet;

final class TransitionMultiMap
{
    private final Map<Character, Set<PlannedTransition>> expectedTransitions;
    private final Set<SpontaneousTransition> spontaneousTransitions;
    private final Set<Character> alphabet;
    private final Set<WildcardTransition> wildcards;

    private TransitionMultiMap(Map<Character, Set<PlannedTransition>> expectedTransitions, Set<WildcardTransition> wildcards,
                               Set<SpontaneousTransition> spontaneousTransitions, Set<Character> alphabet)
    {
        this.wildcards = unmodifiableSet(wildcards);
        this.expectedTransitions = expectedTransitions;
        this.spontaneousTransitions = unmodifiableSet(spontaneousTransitions);
        this.alphabet = unmodifiableSet(alphabet);
    }

    public static TransitionMultiMap build(Set<Transition> transitions)
    {
        Map<Character, Set<PlannedTransition>> expectedTransitions = new HashMap<>();
        Set<SpontaneousTransition> spontaneousTransitions = new HashSet<>();
        Set<Character> expectedChars = new HashSet<>();
        Set<WildcardTransition> wildcards = new HashSet<>();

        for (Transition t : transitions)
        {
            if (t instanceof SpontaneousTransition)
            {
                spontaneousTransitions.add((SpontaneousTransition)t);
            }
            else if (t instanceof CharacterTransition)
            {
                CharacterTransition et = (CharacterTransition)t;
                Set<PlannedTransition> expected = expectedTransitions.computeIfAbsent(et.getWith(), k -> new HashSet<>());
                expected.add(et);
                expectedChars.add(et.getWith());
            }
            else if (t instanceof WildcardTransition)
            {
                wildcards.add((WildcardTransition)t);
            }
            else
            {
                throw new UnsupportedOperationException("Unknown transition type!");
            }
        }
        return new TransitionMultiMap(expectedTransitions, wildcards, spontaneousTransitions, expectedChars);
    }

    public Set<PlannedTransition> getTransitionsFor(char c)
    {
        return getTransitionsFor(c, getWildcards());
    }

    @SuppressWarnings("unchecked")
    public Set<PlannedTransition> getTransitionsFor(char c, Set<? extends PlannedTransition> def)
    {
        Set<PlannedTransition> transitions = expectedTransitions.get(c);
        if (transitions == null)
        {
            return (Set<PlannedTransition>)def;
        }
        return transitions;
    }

    public Set<WildcardTransition> getWildcards()
    {
        return this.wildcards;
    }

    public Set<SpontaneousTransition> getSpontaneousTransitions()
    {
        return this.spontaneousTransitions;
    }

    public Set<Character> getAlphabet()
    {
        return this.alphabet;
    }

    @Override
    public String toString()
    {
        return "Σ = " + getAlphabet() + ", ε-δ = " + getSpontaneousTransitions() + ", δ = " + this.expectedTransitions;
    }
}
