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
package tel.schich.automata.transition;

import tel.schich.automata.State;

public abstract class Transition
{
    private final State origin;
    private final State destination;

    public Transition(State origin, State destination)
    {
        if (origin == null)
        {
            throw new NullPointerException("origin");
        }
        if (destination == null)
        {
            throw new NullPointerException("destination");
        }
        this.origin = origin;
        this.destination = destination;
    }

    public State getOrigin()
    {
        return this.origin;
    }

    public State getDestination()
    {
        return this.destination;
    }

    public abstract String getLabel();

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Transition))
        {
            return false;
        }

        Transition that = (Transition)o;

        if (!destination.equals(that.destination))
        {
            return false;
        }
        if (!origin.equals(that.origin))
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return getOrigin() + " --" + getLabel() + "-->  " + getDestination();
    }

    @Override
    public int hashCode()
    {
        int result = origin.hashCode();
        result = 31 * result + destination.hashCode();
        return result;
    }
}
