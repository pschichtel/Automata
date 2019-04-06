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
package tel.schich.automata.eval;

import java.util.HashSet;
import java.util.Set;
import tel.schich.automata.DFA;
import tel.schich.automata.FiniteAutomaton;
import tel.schich.automata.NFA;
import tel.schich.automata.transition.Transition;

public class Evaluator
{
    @SafeVarargs
    public static StateMachineEvaluator eval(FiniteAutomaton<? extends Transition>... automata)
    {
        if (automata.length == 0)
        {
            throw new IllegalArgumentException("No automaton given!");
        }
        if (automata.length == 1)
        {
            return evaluatorFor(automata[0]);
        }
        final Set<StateMachineEvaluator> evaluators = new HashSet<>();
        for (final FiniteAutomaton<? extends Transition> automaton : automata)
        {
            evaluators.add(evaluatorFor(automaton));
        }
        return new MultiEvaluator(evaluators);
    }

    private static StateMachineEvaluator evaluatorFor(FiniteAutomaton<? extends Transition> automaton)
    {
        if (automaton instanceof DFA)
        {
            return new DFAEvaluator((DFA)automaton);
        }
        if (automaton instanceof NFA)
        {
            return new NFAEvaluator((NFA)automaton);
        }
        throw new IllegalArgumentException("Unknown automaton type: " + automaton.getClass());
    }
}
