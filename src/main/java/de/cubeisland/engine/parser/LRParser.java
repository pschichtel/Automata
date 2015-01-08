/**
 * The MIT License
 * Copyright (c) 2013 Cube Island
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
package de.cubeisland.engine.parser;

import java.util.Set;
import de.cubeisland.engine.parser.grammar.AugmentedGrammar;
import de.cubeisland.engine.parser.parser.Parser;
import de.cubeisland.engine.parser.rule.token.TokenStream;

public class LRParser implements Parser
{
    private final AugmentedGrammar grammar;
    private final Set<ParseState> states;
    private final GotoTable gotoTable;
    private final ActionTable actionTable;

    public LRParser(AugmentedGrammar grammar, Set<ParseState> states, GotoTable gotoTable, ActionTable actionTable)
    {
        this.grammar = grammar;
        this.states = states;
        this.gotoTable = gotoTable;
        this.actionTable = actionTable;
    }

    public AugmentedGrammar getGrammar()
    {
        return grammar;
    }

    public Set<ParseState> getStates()
    {
        return states;
    }

    public GotoTable getGotoTable()
    {
        return gotoTable;
    }

    public ActionTable getActionTable()
    {
        return actionTable;
    }

    public boolean parse(TokenStream tokens)
    {
        // TODO implement me
        return false;
    }
}