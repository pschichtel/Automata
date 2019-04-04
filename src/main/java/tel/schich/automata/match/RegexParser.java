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
package tel.schich.automata.match;

import java.util.LinkedList;
import tel.schich.automata.input.CharacterStream;
import tel.schich.automata.DFA;
import tel.schich.automata.FiniteAutomate;
import tel.schich.automata.NFA;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.input.source.CharSequenceSource;

import static tel.schich.automata.match.PatternParser.bakeAutomate;
import static tel.schich.automata.match.PatternParser.readCharacter;

public abstract class RegexParser
{
    private RegexParser()
    {}

    public static DFA toDFA(String regex)
    {
        return toNFA(regex).toDFA();
    }

    public static NFA toNFA(String regex)
    {
        return readExpression(new CharacterStream(new CharSequenceSource(regex)), 0);
    }

    private static NFA readExpression(CharacterStream stream, int depth)
    {
        LinkedList<FiniteAutomate<? extends Transition>> elements = new LinkedList<>();

        for (final char c : stream)
        {
            switch (c)
            {
                case '(':
                    elements.addLast(readExpression(stream, depth + 1));
                    break;
                case ')':
                    if (depth > 0)
                    {
                        break;
                    }
                case '|':
                    NFA left = bakeAutomate(elements);
                    NFA right = readExpression(stream, depth);
                    elements.clear();
                    elements.add(left.or(right));
                    break;
                case '*':
                    if (!elements.isEmpty())
                    {
                        elements.addLast(elements.removeLast().kleeneStar());
                        break;
                    }
                default:
                    elements.addLast(readCharacter(stream, true));
            }
        }
        return bakeAutomate(elements);
    }
}
