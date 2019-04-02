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
package tel.schich.automata.rule.token.automate.eval;

import java.util.Set;
import tel.schich.automata.rule.token.automate.NFA;
import tel.schich.automata.rule.token.automate.State;

public class NFAEvaluator implements StateMachineEvaluator
{
    private final NFA automate;
    private Set<State> currentStates;
    private boolean currentlyAccepting;

    public NFAEvaluator(NFA automate)
    {
        this.automate = automate;
        this.currentStates = automate.getStartStates();
        this.currentlyAccepting = automate.isAccepting(this.currentStates);
    }

    @Override
    public boolean transition(char c)
    {
        this.currentStates = this.automate.transition(this.currentStates, c);
        this.currentlyAccepting = this.automate.isAccepting(this.currentStates);
        return isCurrentAccepting();
    }

    @Override
    public boolean isCurrentAccepting()
    {
        return this.currentlyAccepting;
    }

    @Override
    public String toString()
    {
        return this.currentStates.toString();
    }
}
