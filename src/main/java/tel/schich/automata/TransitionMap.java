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
import java.util.Map;
import java.util.Set;
import tel.schich.automata.transition.CharacterTransition;
import tel.schich.automata.transition.ExpectedTransition;
import tel.schich.automata.transition.WildcardTransition;

public class TransitionMap
{

    private final Map<Character, CharacterTransition> charTransitions;
    private final WildcardTransition wildcard;

    public TransitionMap(Map<Character, CharacterTransition> charTransitions, WildcardTransition wildcard)
    {
        this.charTransitions = charTransitions;
        this.wildcard = wildcard;
    }

    public static TransitionMap build(Set<ExpectedTransition> transitions)
    {
        final Map<Character, CharacterTransition> charTransitions = new HashMap<Character, CharacterTransition>();
        WildcardTransition wildcard = null;

        for (final ExpectedTransition t : transitions)
        {
            if (t instanceof CharacterTransition)
            {
                CharacterTransition ct = (CharacterTransition)t;
                charTransitions.put(ct.getWith(), ct);
            }
            else if (t instanceof WildcardTransition)
            {
                if (wildcard != null)
                {
                    throw new IllegalArgumentException("There was more than one wildcard exception!");
                }
                wildcard = (WildcardTransition)t;
            }
            else
            {
                throw new UnsupportedOperationException("Unknown transition type!");
            }
        }

        return new TransitionMap(charTransitions, wildcard);
    }

    public ExpectedTransition getTransitionFor(char c)
    {
        return getTransitionFor(c, getWildcard());
    }

    ExpectedTransition getTransitionFor(char c, ExpectedTransition def)
    {
        ExpectedTransition transition = this.charTransitions.get(c);
        if (transition == null)
        {
            return def;
        }
        return transition;
    }

    public WildcardTransition getWildcard()
    {
        return wildcard;
    }
}
