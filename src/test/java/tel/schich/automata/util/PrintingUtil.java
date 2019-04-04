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

import java.util.Set;

import tel.schich.automata.FiniteAutomate;
import tel.schich.automata.State;
import tel.schich.automata.transition.Transition;

public abstract class PrintingUtil
{
    private PrintingUtil()
    {}

    public static void printAutomoton(String name, FiniteAutomate<? extends Transition> a)
    {
        System.out.println(name + ":");
        System.out.println("States:      " + a.getStates());
        System.out.println("Transitions: " + a.getTransitions());
        System.out.println("Accepting:   " + a.getAcceptingStates());
        System.out.println("Start:       " + a.getStartState());
        System.out.println();
    }

    public static void automatonToDot(String name, FiniteAutomate<? extends Transition> a)
    {
        StringBuilder out = new StringBuilder(
                "digraph {\n"
                + "\trankdir=LR;\n");

        out.append("\tlabel=\"").append(name).append("\"\n");
        out.append("\tinit [style=invis;width=0.1;height=0.1;margin=0.1];\n\n");

        Set<State> accepting = a.getAcceptingStates();
        for (State s : a.getStates()) {
            out.append("\t\"").append(s.getLabel()).append("\" [shape=");
            if (accepting.contains(s)) {
                out.append("doublecircle");
            } else {
                out.append("circle");
            }
            out.append("];\n");
        }
        out.append('\n');
        out.append("\tinit->\"").append(a.getStartState().getLabel()).append("\";\n");

        for (Transition t : a.getTransitions()) {
            out.append("\t");
            out.append('"').append(t.getOrigin().getLabel()).append("\"->\"").append(t.getDestination().getLabel()).append('"');
            out.append(" [label=\"");
            out.append(t.getLabel());
            out.append("\"];\n");
        }

        out.append("}\n");

        System.out.println(out);
    }
}
