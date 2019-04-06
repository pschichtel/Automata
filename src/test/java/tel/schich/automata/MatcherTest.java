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
package tel.schich.automata;

import java.util.regex.Pattern;

import tel.schich.automata.match.Matcher;
import tel.schich.automata.util.PrintingUtil;
import org.junit.Test;

public class MatcherTest
{

    @Test
    public void testRead()
    {
        DFA a = Matcher.matchAll('a');

        PrintingUtil.printAutomoton("Read char", a);
    }

    @Test
    public void testAnd()
    {
        DFA a = Matcher.matchAll('a');
        DFA b = Matcher.matchAll('b');

        NFA c = a.concat(b);
        PrintingUtil.printAutomoton("And", c);
    }

    @Test
    public void testOr()
    {
        DFA a = Matcher.matchAll('a');
        DFA b = Matcher.matchAll('b');

        NFA c = a.concat(b);
        PrintingUtil.printAutomoton("Or", c);
    }

    @Test
    public void testKleene()
    {
        DFA a = Matcher.matchAll('a');

        NFA b = a.kleeneStar();
        PrintingUtil.printAutomoton("Kleene", b);
    }

    @Test
    public void testPattern()
    {
        Pattern p = Pattern.compile("[ab]*");
        System.out.println("Pattern.toString(): " + p.toString());

        NFA aPlus = Matcher.match(p).toNFA();
        PrintingUtil.printAutomoton(p.toString() + " NFA", aPlus);

        DFA aPlusD = aPlus.toDFA().minimize();
        PrintingUtil.printAutomoton(p.toString() + " DFA", aPlusD);
    }
}
