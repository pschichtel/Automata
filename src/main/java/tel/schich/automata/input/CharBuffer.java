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
package tel.schich.automata.input;

public final class CharBuffer
{
    private Node hook;
    private Node pointer;
    private int size = 0;
    private int distanceToHook = -1;

    /**
     * Offers a new element to the buffer.
     * O(1)
     *
     * @param c the new element
     */
    public void offer(char c)
    {
        // the simple case: no nodes hooked in yet
        if (this.hook == null)
        {
            this.hook = new Node(c);
            this.size = 1;
            System.out.println(this);
            return;
        }

        final Node second = this.hook;
        final Node last = second.previous;

        this.hook = new Node(c);
        this.hook.next = second;
        this.hook.previous = last;

        second.previous = this.hook;
        last.next = this.hook;

        this.size++;
        if (this.pointer != null)
        {
            this.distanceToHook++;
        }
        System.out.println(this);
    }

    private void clean()
    {
        while (hook.previous != hook && hook.previous != pointer && hook.previous.checkpointCounter <= 0)
        {
            removeLast();
        }
    }

    /**
     * Returns the current element.
     * O(1)
     *
     * @return the current element
     */
    public char current()
    {
        notEmpty();
        return this.pointer.value;
    }

    /**
     * Advances the pointer to the current element to the next one.
     * O(1)
     */
    public void advance()
    {
        notEmpty();
        if (this.pointer == null)
        {
            this.pointer = this.hook.previous;
            this.distanceToHook = this.size - 1; // we initialize to the tail, so the maximum distance is size - 1
            return;
        }
        if (this.pointer == this.hook)
        {
            throw new IndexOutOfBoundsException("Nothing to advance to!");
        }
        this.pointer = this.pointer.previous;
        this.distanceToHook--;
        clean();
    }

    /**
     * Creates a checkpoint for this current element.
     * Checkpoints will keep the element in the buffer until the checkpoint is dropped.
     * This allows jumping back to that element.
     * O(1)
     *
     * @return a Checkpoint instance for the current element
     */
    public Checkpoint checkpoint()
    {
        return new Checkpoint(this.pointer);
    }

    /**
     * Lookups elements behind the current element (elements that have been added after the current element)
     * O(n)
     *
     * @param n the number of elements to peek ahead of the current element
     *
     * @return the element that is n positions behind the current element
     */
    public char peekAhead(int n)
    {
        // peeking means going backwards in the circle
        Node node = this.hook.previous;
        while (n > 0 && node != this.hook)
        {
            n--;
            node = node.previous;
        }
        if (n > 0)
        {
            throw new IndexOutOfBoundsException("The n was to big, reached the end of the buffered input");
        }
        return node.value;
    }

    /**
     * Removes the last element.
     * Last in the case means the oldest element (which is the element in front of the hook element)
     * O(1)
     */
    public void removeLast()
    {
        notEmpty();

        // don't delete the hook
        if (this.hook == this.hook.previous)
        {
            return;
        }

        final Node tail = this.hook.previous;
        final Node newTail = tail.previous;
        newTail.next = this.hook;
        this.hook.previous = newTail;

        this.size--;
    }

    /**
     * This returns the number of readable elements.
     * This does not equal the size of the buffer. The number of readable elements is the number of
     * elements in front of the current node (the distance to the hook element)
     * O(1)
     *
     * @return the number of readable elements
     */
    public int readable()
    {
        return this.distanceToHook;
    }

    /**
     * Checks whether there are readable elements in the buffer.
     * O(1)
     *
     * @return true if there is at least one readable element.
     */
    public boolean hasReadableElements()
    {
        return readable() > 0;
    }

    /**
     * Returns the number of elements in the buffer. After the first element has been added, this will
     * always be &gt;= 1.
     * O(1)
     *
     * @return the number of elements in the buffer
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Checks whether this buffer is empty.
     * As this buffer can't remove its last element, this method will only ever return true,
     * when nothing has been added to the buffer yet.
     * O(1)
     *
     * @return true only if nothing was ever written to the buffer
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    private void notEmpty()
    {
        if (this.hook == null)
        {
            throw new IllegalStateException("The queue is empty!");
        }
    }

    /**
     * The toString() overload shows the values and directions in both directions.
     * O(2n)
     *
     * @return A string representation showing all connections
     */
    @Override
    public String toString()
    {
        if (this.hook == null)
        {
            return "[]";
        }
        final StringBuilder builder = new StringBuilder();
        Node node;

        builder.append("<[").append(this.hook.value).append("]>");
        node = this.hook.next;
        while (node != this.hook)
        {
            builder.append(" --> ").append(node.value);
            if (node == this.pointer)
            {
                builder.append('*');
            }
            if (node.checkpointCounter > 0)
            {
                builder.append('!');
            }
            node = node.next;
        }
        builder.append(" --> ").append('[').append(node.value).append(']');

        node = this.hook.previous;
        while (node != this.hook)
        {
            builder.insert(0, " <-- ").insert(0, node.value);
            if (node == this.pointer)
            {
                builder.insert(0, '*');
            }
            if (node.checkpointCounter > 0)
            {
                builder.insert(0, '!');
            }
            node = node.previous;
        }
        builder.insert(0, " <-- ").insert(0, ']').insert(0, node.value).insert(0, '[');

        return builder.toString();
    }

    private final static class Node
    {
        private final char value;
        public Node previous;
        public Node next;
        public int checkpointCounter = 0;

        public Node(char value)
        {
            this.value = value;
            this.previous = this;
            this.next = this;
        }
    }

    public class Checkpoint
    {
        private Node node;

        public Checkpoint(Node node)
        {
            this.node = node;
            node.checkpointCounter++;
        }

        public void jumpBack()
        {
            CharBuffer.this.pointer = this.node;
        }

        public void restore()
        {
            jumpBack();
            drop();
        }

        public void drop()
        {
            this.node.checkpointCounter--;
            this.node = null;
            CharBuffer.this.clean();
        }
    }
}
