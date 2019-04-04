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

public class UnorderedPair<L, R> implements Pair<L, R>
{
    private final L left;
    private final R right;

    public UnorderedPair(L left, R right)
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

    /**
     * This implementation ignores the order of the fields, so &lt;a, b&gt; = &lt;b, a&gt;
     *
     * @param o the other object
     *
     * @return true of they're the same
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof UnorderedPair))
        {
            return false;
        }

        UnorderedPair that = (UnorderedPair)o;

        if (left.equals(that.left) && right.equals(that.right))
        {
            return true;
        }
        if (left.equals(that.right) && right.equals(that.left))
        {
            return true;
        }
        return false;
    }

    /**
     * This implementation just adds up both hashCodes to drop ordering
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return left.hashCode() + right.hashCode();
    }

    @Override
    public String toString()
    {
        return "<" + this.left + ", " + this.right + ">";
    }
}
