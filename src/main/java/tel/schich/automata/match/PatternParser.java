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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import tel.schich.automata.input.CharacterStream;
import tel.schich.automata.DFA;
import tel.schich.automata.FiniteAutomate;
import tel.schich.automata.NFA;
import tel.schich.automata.transition.Transition;
import tel.schich.automata.input.source.CharSequenceSource;
import tel.schich.automata.util.Util;
import tel.schich.automata.input.CharBuffer;

import static tel.schich.automata.match.Matcher.*;

public abstract class PatternParser
{
    private PatternParser()
    {}

    public static DFA toDFA(String pattern)
    {
        return toNFA(pattern).toDFA();
    }

    public static NFA toNFA(String pattern)
    {
        return readExpression(new CharacterStream(new CharSequenceSource(pattern)), 0);
    }

    private static NFA readExpression(CharacterStream stream, int depth)
    {
        LinkedList<FiniteAutomate<? extends Transition>> elements = new LinkedList<>();

        for (final char c : stream)
        {
            switch (c)
            {
                case '[':
                    elements.addLast(readCharacterClass(stream, 0));
                    break;
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
                case '+':
                case '*':
                case '{':
                case '?':
                    if (!elements.isEmpty())
                    {
                        elements.addLast(readQuantifier(stream, elements.removeLast()));
                    }
                    else
                    {
                        elements.addLast(readCharacter(stream, false));
                    }
                    break;
                case '.':
                    elements.add(matchWildcard());
                    break;
                default:
                    elements.addLast(readCharacter(stream, true));
            }
        }
        return bakeAutomate(elements);
    }

    static NFA bakeAutomate(LinkedList<FiniteAutomate<? extends Transition>> elements)
    {
        if (elements.isEmpty())
        {
            return NFA.EMPTY;
        }
        NFA automate = elements.getFirst().toNFA();
        for (final FiniteAutomate<? extends Transition> element : elements.subList(1, elements.size()))
        {
            automate = automate.and(element);
        }

        return automate;
    }

    private static NFA readQuantifier(CharacterStream s, FiniteAutomate<? extends Transition> automate)
    {
        switch (s.current())
        {
            case '*':
                return automate.kleeneStar();
            case '+':
                return automate.kleenePlus();
            case '?':
                return automate.or(NFA.EPSILON);
            case '{':
                return readSpecificQuantifier(s, automate);
            default:
                return automate.toNFA();
        }
    }

    private static NFA readSpecificQuantifier(CharacterStream s, FiniteAutomate<? extends Transition> automate)
    {
        final CharBuffer.Checkpoint checkpoint = s.checkpoint();

        if (s.canPeekAhead() && Character.isDigit(s.peekAhead()))
        {
            int min = readNumber(s, NumberSyntax.DECIMAL);
            char c = s.current();

            if (c == '}')
            {
                checkpoint.drop();
                return automate.repeat(min);
            }
            else if (c == ',' && s.canPeekAhead())
            {
                char peeked = s.peekAhead();
                if (Character.isDigit(peeked))
                {
                    int max = readNumber(s, NumberSyntax.DECIMAL);
                    if (s.current() == '}')
                    {
                        checkpoint.drop();
                        s.advance();
                        return automate.repeatMinMax(min, max);
                    }
                }
                else if (peeked == '}')
                {
                    checkpoint.drop();
                    return automate.repeatMin(min);
                }
            }
        }

        checkpoint.restore();

        return automate.and(readCharacter(s, true));
    }

    private static int readNumber(CharacterStream s, NumberSyntax syntax)
    {
        StringBuilder buf = new StringBuilder();
        for (final char c : s)
        {
            if (syntax.accept(c))
            {
                buf.append(c);
            }
        }

        return Integer.valueOf(buf.toString(), syntax.getBase());
    }

    private static DFA readCharacterClass(CharacterStream s, int depth)
    {
        NFA automate = NFA.EMPTY;

        if (!s.canPeekAhead(2))
        {
            return readCharacter(s, true);
        }
        boolean negative = s.peekAhead() == '^';

        final CharBuffer.Checkpoint checkpoint = s.checkpoint();
        if (negative)
        {
            s.advance();
        }
        if (s.peekAhead() == ']')
        {
            checkpoint.restore();
            return readCharacter(s, true);
        }

        boolean hasEnded = false;
        for (final char c : s)
        {
            if (c == ']')
            {
                hasEnded = true;
                break;
            }
            if (c == '[')
            {
                automate = automate.or(readCharacterClass(s, depth + 1));
            }
            else
            {
                automate = automate.or(readCharacter(s, false));
            }
        }

        if (!hasEnded)
        {
            checkpoint.restore();
            return readCharacter(s, true);
        }
        checkpoint.drop();
        if (negative)
        {
            return automate.complement();
        }
        return automate.toDFA();
    }

    static DFA readCharacter(CharacterStream s, boolean allowQuote)
    {
        char c = s.current();
        if (c == '\\')
        {
            return readEscapeSequence(s, allowQuote);
        }
        return matchOne(c);
    }

    private static DFA readEscapeSequence(CharacterStream s, boolean allowQuote)
    {
        switch (s.next())
        {
            case 't':
                return matchOne('\t');
            case 'n':
                return matchOne('\n');
            case 'r':
                return matchOne('\r');
            case 'f':
                return matchOne('\f');
            case 'a':
                return matchOne('\u0007');
            case 'e':
                return matchOne('\u001B');
            case 's':
                return matchOne(' ', '\t', '\n', (char)0xB, '\f', '\r');
            case '\\':
                return matchOne('\\');
            case 'd':
                return matchOne('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
            case 'w':
                List<Character> chars = new ArrayList<>();
                for (int i = 'a'; i <= 'z'; ++i)
                {
                    chars.add((char)i);
                }
                for (int i = 'A'; i <= 'Z'; ++i)
                {
                    chars.add((char)i);
                }
                chars.add('_');
                for (int i = '0'; i <= '9'; ++i)
                {
                    chars.add((char)i);
                }
                return matchOne(Util.convertCharCollectionToArray(chars));
            case 'R':
                return matchAll('\r', '\n').or(matchOne('\n', '\u000B', '\u000C', '\r', '\u0085', '\u2028',
                                                        '\u2029')).toDFA();
            case '0':
                return matchOne((char)readNumber(s, NumberSyntax.OCTAL));
            case 'x':
                return matchOne((char)readNumber(s, NumberSyntax.HEXADECIMAL));
            case 'Q':
                if (allowQuote)
                {
                    return readQuoted(s);
                }
            default:
                return readCharacter(s, allowQuote);
        }
    }

    private static DFA readQuoted(CharacterStream s)
    {
        LinkedList<FiniteAutomate<? extends Transition>> elems = new LinkedList<>();
        final CharBuffer.Checkpoint checkpoint = s.checkpoint();
        for (final Character c : s)
        {
            if (c == '\\' && s.canPeekAhead() && s.peekAhead() == 'E')
            {
                s.advance();
                checkpoint.drop();
                return bakeAutomate(elems).toDFA();
            }
            elems.add(readCharacter(s, false));
        }

        checkpoint.restore();
        return readCharacter(s, true);
    }

    private enum NumberSyntax
    {
        OCTAL(8, c -> c >= '0' && c <= '7'),
        DECIMAL(10, c -> c >= '0' && c <= '9'),
        HEXADECIMAL(16, c -> DECIMAL.accept(c) || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F');

        private final int base;
        private final Predicate<Character> accept;

        NumberSyntax(int base, Predicate<Character> accept)
        {
            this.base = base;
            this.accept = accept;
        }

        public int getBase()
        {
            return base;
        }

        public boolean accept(char c) {
            return accept.test(c);
        }
    }
}
