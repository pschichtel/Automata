/**
 * The MIT License
 * Copyright (c) 2014 Cube Island
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
package de.cubeisland.engine.parser.rule.token.automate.transition;

import de.cubeisland.engine.parser.rule.token.automate.State;

public class CharacterTransition extends ExpectedTransition
{
    private final char with;

    public CharacterTransition(State origin, char with, State destination)
    {
        super(origin, destination);
        this.with = with;
    }

    public char getWith()
    {
        return this.with;
    }

    @Override
    public String getLabel()
    {
        return "'" + getWith() + "'";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof CharacterTransition))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        CharacterTransition that = (CharacterTransition) o;

        return this.with == that.with;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) with;
        return result;
    }
}
