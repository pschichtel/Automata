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
import tel.schich.automata.FiniteAutomate;
import tel.schich.automata.NFA;
import tel.schich.automata.transition.Transition;

public class Evaluator
{
    public static StateMachineEvaluator eval(FiniteAutomate<? extends Transition>... automates)
    {
        if (automates.length == 0)
        {
            throw new IllegalArgumentException("No automate given!");
        }
        if (automates.length == 1)
        {
            return evaluatorFor(automates[0]);
        }
        final Set<StateMachineEvaluator> evaluators = new HashSet<>();
        for (final FiniteAutomate<? extends Transition> automate : automates)
        {
            evaluators.add(evaluatorFor(automate));
        }
        return new MultiEvaluator(evaluators);
    }

    private static StateMachineEvaluator evaluatorFor(FiniteAutomate<? extends Transition> automate)
    {
        if (automate instanceof DFA)
        {
            return new DFAEvaluator((DFA)automate);
        }
        if (automate instanceof NFA)
        {
            return new NFAEvaluator((NFA)automate);
        }
        throw new IllegalArgumentException("Unknown automate type: " + automate.getClass());
    }
}
