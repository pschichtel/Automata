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
package de.cubeisland.engine.parser.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import de.cubeisland.engine.parser.TestGrammars;
import de.cubeisland.engine.parser.Variable;
import de.cubeisland.engine.parser.rule.token.TokenSpec;
import junit.framework.TestCase;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

public class BaseGrammarTest extends TestCase
{


    Grammar g = TestGrammars.SIMPLE_EXPR_WITH_EPS;
    Grammar h = TestGrammars.SIMPLE_EXPR;

    public void testIsNullable() throws Exception
    {
        System.out.println(g);

        for (Variable var : g.getVariables())
        {
            if (var.getName() == "expr")
            {
                assertThat("Variable expr is nullable.", g.isNullable(var), is(true));
            }
            else
            {
                assertThat("Variable " + var.getName() + " is not nullable.", g.isNullable(var), is(false));
            }
        }
    }

    public void testFirst() throws Exception
    {
        // TODO add some fancy magic to the test
        System.out.println("test");
        for (Variable var : h.getVariables())
        {
            //System.out.println(var + " --> " + h.first(2, var));
        }
        assertThat("dummy", true, is(true));
    }
}