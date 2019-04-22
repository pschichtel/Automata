package tel.schich.automata.util;

import tel.schich.automata.FiniteAutomaton;
import tel.schich.automata.transition.Transition;

public class TestPrinting {
    public static void automatonToDot(String name, FiniteAutomaton<? extends Transition> a)
    {
        System.out.println(PrintingUtil.automatonToDot(name, a));
    }

    public static void printAutomoton(String name, FiniteAutomaton<? extends Transition> a)
    {
        System.out.println(name + ":");
        System.out.println("States:      " + a.getStates());
        System.out.println("Transitions: " + a.getTransitions());
        System.out.println("Accepting:   " + a.getAcceptingStates());
        System.out.println("Start:       " + a.getStartState());
        System.out.println();
    }
}
