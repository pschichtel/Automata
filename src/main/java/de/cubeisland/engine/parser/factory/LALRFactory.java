package de.cubeisland.engine.parser.factory;

import de.cubeisland.engine.parser.LRParser;
import de.cubeisland.engine.parser.factory.result.CompilationResult;
import de.cubeisland.engine.parser.factory.result.SuccessfulResult;
import de.cubeisland.engine.parser.grammar.AugmentedGrammar;

import static de.cubeisland.engine.parser.factory.result.CompilationResult.success;

public class LALRFactory extends LRFactory
{
    public CompilationResult<LRParser> produce(AugmentedGrammar g)
    {
        CompilationResult<LRParser> result = super.produce(g);
        if (result instanceof SuccessfulResult)
        {
            return optimize(((SuccessfulResult<LRParser>)result).getParser());
        }
        return result;
    }

    protected CompilationResult<LRParser> optimize(LRParser parser)
    {
        return success(parser);
    }
}