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
package de.cubeisland.engine.parser.rule.token;

import java.io.IOException;
import java.util.Iterator;
import de.cubeisland.engine.parser.rule.token.CharBuffer.Checkpoint;

public final class CharacterStream implements Iterator<Character>, Iterable<Character>
{
    private final InputSource source;
    private CharBuffer buffer = new CharBuffer();

    public CharacterStream(InputSource source)
    {
        this.source = source;
    }

    @Override
    public Iterator<Character> iterator()
    {
        return this;
    }

    @Override
    public boolean hasNext()
    {
        try
        {
            return !source.isDepleted();
        }
        catch (IOException e)
        {
            throw new SourceReadException(e);
        }
    }

    private boolean ensureReadableChar()
    {
        return buffer.hasReadableElements() || readIntoBuffer(1) == 1;
    }

    private int readIntoBuffer(int n)
    {
        int i;
        for (i = 0; i < n && hasNext(); ++i)
        {
            try
            {
                this.buffer.offer(source.read());
            }
            catch (SourceDepletedException ignored) // will not be thrown due to hasNext() check
            {
            }
            catch (IOException e)
            {
                throw new SourceReadException(e);
            }
        }
        return i;
    }

    @Override
    public Character next()
    {
        if (!ensureReadableChar())
        {
            throw new IllegalStateException("Source is depleted!");
        }
        this.buffer.advance();
        return this.buffer.current();
    }

    public boolean canPeekAhead()
    {
        return canPeekAhead(1);
    }

    public boolean canPeekAhead(int n)
    {
        if (buffer.readable() >= n)
        {
            return true;
        }
        n -= buffer.readable();
        return readIntoBuffer(n) == n;
    }

    public char peekAhead()
    {
        return peekAhead(1);
    }

    public char peekAhead(int n)
    {
        if (!canPeekAhead(n))
        {
            throw new StringIndexOutOfBoundsException("Can't peek that far: " + n);
        }
        return this.buffer.peekAhead(n);
    }

    public char current()
    {
        if (isUninitialized())
        {
            throw new IllegalStateException("You can't call current() before invoking next() at least once!");
        }
        return this.buffer.current();
    }

    @Override
    public void remove()
    {
        advance();
    }

    public CharacterStream advance()
    {
        if (hasNext())
        {
            buffer.advance();
        }
        return this;
    }

    public boolean isUninitialized()
    {
        return this.buffer.isEmpty();
    }

    public boolean isDepleted()
    {
        return !canPeekAhead();
    }

    public Checkpoint checkpoint()
    {
        return this.buffer.checkpoint();
    }

    public static class SourceReadException extends RuntimeException
    {
        public SourceReadException(Throwable cause)
        {
            super(cause);
        }
    }

    public static class SourceDepletedException extends Exception
    {
        public SourceDepletedException(String message)
        {
            super(message);
        }

        public SourceDepletedException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public SourceDepletedException(Throwable cause)
        {
            super(cause);
        }
    }
}
