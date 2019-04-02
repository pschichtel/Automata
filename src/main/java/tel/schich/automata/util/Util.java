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
package tel.schich.automata.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import tel.schich.automata.util.OrderedPair;
import tel.schich.automata.util.UnorderedPair;

public abstract class Util
{
    private Util()
    {
    }


    public static <T> Set<T> asSet(T... elements)
    {
        return new HashSet<>(Arrays.asList(elements));
    }

    public static <L, R> Set<OrderedPair<L, R>> orderedMultiply(Set<L> left, Set<R> right)
    {
        Set<OrderedPair<L, R>> out = new HashSet<OrderedPair<L, R>>();

        for (L l : left)
        {
            for (R r : right)
            {
                out.add(new OrderedPair<L, R>(l, r));
            }
        }

        return out;
    }

    public static <L, R> Set<UnorderedPair<L, R>> unorderedMultiply(Set<L> left, Set<R> right)
    {
        Set<UnorderedPair<L, R>> out = new HashSet<UnorderedPair<L, R>>();

        for (L l : left)
        {
            for (R r : right)
            {
                out.add(new UnorderedPair<L, R>(l, r));
            }
        }

        return out;
    }

    public static char[] convertCharCollectionToArray(Collection<Character> chars)
    {
        final char[] array = new char[chars.size()];
        int i = 0;
        for (final Character c : chars)
        {
            array[i++] = c;
        }

        return array;
    }

    public static <T> Set<T> fixPointIterate(Set<T> in, Function<T, Set<T>> func)
    {
        Set<T> result = new HashSet<T>(in);
        Set<T> newElements = new HashSet<T>(in);

        while (true)
        {
            Set<T> temp = new HashSet<T>();
            for (T element : newElements)
            {
                temp.addAll(func.apply(element));
            }
            newElements = temp;
            if (result.containsAll(newElements))
            {
                return result;
            }
            result.addAll(newElements);
        }
    }
}
