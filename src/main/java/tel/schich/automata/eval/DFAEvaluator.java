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

import tel.schich.automata.DFA;
import tel.schich.automata.State;

public class DFAEvaluator implements StateMachineEvaluator
{
    private final DFA automate;
    private State current;

    public DFAEvaluator(DFA automate)
    {
        this.automate = automate;
        this.current = automate.getStartState();
    }

    @Override
    public boolean transition(char c)
    {
        this.current = this.current.transition(this.automate, c);
        return isCurrentAccepting();
    }

    @Override
    public boolean isCurrentAccepting()
    {
        return this.automate.isAccepting(this.current);
    }

    @Override
    public String toString()
    {
        return this.current.toString();
    }
}
