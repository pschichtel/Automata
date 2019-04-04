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

import java.util.Objects;

public class OrderedPair<L, R> implements Pair<L, R>
{
    private final L left;
    private final R right;

    public OrderedPair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft()
    {
        return left;
    }

    @Override
    public R getRight()
    {
        return right;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof OrderedPair))
        {
            return false;
        }

        OrderedPair that = (OrderedPair)o;

        if (!Objects.equals(left, that.left))
        {
            return false;
        }
        if (!Objects.equals(right, that.right))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
