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
package de.cubeisland.engine.parser.rule.token.automate;

import java.util.regex.Pattern;
import de.cubeisland.engine.parser.rule.token.automate.match.Matcher;
import de.cubeisland.engine.parser.util.PrintingUtil;
import org.junit.Test;

public class MatcherTest
{

    @Test
    public void testRead() throws Exception
    {
        DFA a = Matcher.matchAll('a');

        PrintingUtil.printAutomate("Read char", a);
    }

    @Test
    public void testAnd() throws Exception
    {
        DFA a = Matcher.matchAll('a');
        DFA b = Matcher.matchAll('b');

        NFA c = a.and(b);
        PrintingUtil.printAutomate("And", c);
    }

    @Test
    public void testOr() throws Exception
    {
        DFA a = Matcher.matchAll('a');
        DFA b = Matcher.matchAll('b');

        NFA c = a.and(b);
        PrintingUtil.printAutomate("Or", c);
    }

    @Test
    public void testKleene() throws Exception
    {
        DFA a = Matcher.matchAll('a');

        NFA b = a.kleeneStar();
        PrintingUtil.printAutomate("Kleene", b);
    }

    @Test
    public void testPattern() throws Exception
    {
        Pattern p = Pattern.compile("[ab]*");
        System.out.println("Pattern.toString(): " + p.toString());

        NFA aPlus = Matcher.match(p).toNFA();
        PrintingUtil.printAutomate(p.toString() + " NFA", aPlus);

        DFA aPlusD = aPlus.toDFA().minimize();
        PrintingUtil.printAutomate(p.toString() + " DFA", aPlusD);
    }
}
