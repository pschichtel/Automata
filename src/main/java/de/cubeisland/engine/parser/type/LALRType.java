package de.cubeisland.engine.parser.type;

import de.cubeisland.engine.parser.grammar.AugmentedGrammar;
import de.cubeisland.engine.parser.grammar.CompiledGrammar;

public class LALRType extends LRType
{
    public CompiledGrammar compile(AugmentedGrammar g)
    {
        CompiledGrammar cg = super.compile(g);
        return optimize(cg);
    }

    protected CompiledGrammar optimize(CompiledGrammar cg)
    {
        return cg;
    }
}