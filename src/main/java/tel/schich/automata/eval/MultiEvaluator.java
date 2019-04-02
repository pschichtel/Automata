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

public class MultiEvaluator implements StateMachineEvaluator
{
    private final Set<StateMachineEvaluator> evaluators;
    private boolean currentlyAccepting;

    public MultiEvaluator(Set<StateMachineEvaluator> evaluators)
    {
        this.evaluators = new HashSet<StateMachineEvaluator>(evaluators);

        this.currentlyAccepting = true;
        for (final StateMachineEvaluator evaluator : evaluators)
        {
            if (!evaluator.isCurrentAccepting())
            {
                this.currentlyAccepting = false;
                break;
            }
        }
    }

    @Override
    public boolean transition(char c)
    {
        this.currentlyAccepting = true;
        for (final StateMachineEvaluator evaluator : evaluators)
        {
            if (!evaluator.transition(c))
            {
                this.currentlyAccepting = false;
                break;
            }
        }
        return isCurrentAccepting();
    }

    @Override
    public boolean isCurrentAccepting()
    {
        return this.currentlyAccepting;
    }
}
