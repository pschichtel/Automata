package de.cubeisland.engine.parser.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import de.cubeisland.engine.parser.Variable;
import de.cubeisland.engine.parser.rule.Rule;
import de.cubeisland.engine.parser.rule.RuleElement;
import de.cubeisland.engine.parser.rule.token.TokenSpec;
import de.cubeisland.engine.parser.util.tokenConcatter;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

public abstract class BaseGrammar
{
    private final Set<Variable> variables;
    private final Set<TokenSpec> tokens;
    private final Set<Variable> nullables;
    private final List<Rule> rules;
    private final Variable start;

    private final Map<Integer, Map<Variable, Set<List<TokenSpec>>>> firstsCache = new HashMap<Integer, Map<Variable, Set<List<TokenSpec>>>>();

    public BaseGrammar(Set<Variable> variables, Set<TokenSpec> tokens, List<Rule> rules, Variable start)
    {
        this.variables = unmodifiableSet(variables);
        this.tokens = unmodifiableSet(tokens);
        this.rules = unmodifiableList(rules);
        this.start = start;

        this.nullables = nullClosure();

    }

    protected Set<Variable> nullClosure()
    {
        Set<Variable> nullable = new HashSet<Variable>();
        int oldSize = 0;
        int newSize = 0;

        do
        {
            Set<Variable> notNullVariables = new HashSet<Variable>(variables);
            notNullVariables.removeAll(nullable);

            for (Variable variable : notNullVariables)
            {
                Set<Rule> rulesForVar = getRulesFor(variable);
                for (Rule rule : rulesForVar)
                {
                    boolean allNullable = true;
                    for (RuleElement ruleElement : rule.getBody())
                    {
                        if (ruleElement instanceof TokenSpec)
                        {
                            allNullable = false;
                            break;
                        }
                        else if (ruleElement instanceof Variable && !nullable.contains(ruleElement))
                        {
                            allNullable = false;
                            break;
                        }
                    }

                    if (allNullable)
                    {
                        nullable.add(variable);
                        break;
                    }
                }
            }

            oldSize = newSize;
            newSize = nullable.size();
        }
        while (oldSize < newSize);

        return nullable;
    }

    public Set<Variable> getVariables()
    {
        return variables;
    }

    public Set<TokenSpec> getTokens()
    {
        return tokens;
    }

    public List<Rule> getRules()
    {
        return rules;
    }

    public Set<Rule> getRulesFor(Variable head)
    {
        Set<Rule> rules = new HashSet<Rule>();
        for (final Rule rule : this.rules)
        {
            if (rule.getHead() == head)
            {
                rules.add(rule);
            }
        }
        return rules;
    }

    public Set<List<TokenSpec>> first(int k, Variable variable)
    {
        if (!this.firstsCache.containsKey(k))
        {
            this.calculateFirsts(k);
        }

        return this.firstsCache.get(k).get(variable);
    }

    public void calculateFirsts(int k)
    {
        final HashMap<Variable, Set<List<TokenSpec>>> first = new HashMap<Variable, Set<List<TokenSpec>>>();

        this.firstsCache.put(k, first);

        for (Variable variable : this.variables)
        {
            first.put(variable, new HashSet<List<TokenSpec>>());
        }


        int oldHash;
        int newHash = first.hashCode();

        do
        {

            for (Rule rule : this.rules)
            {
                Set<List<TokenSpec>> ruleFirst = new HashSet<List<TokenSpec>>();
                ruleFirst.add(new ArrayList<TokenSpec>());

                for (RuleElement ruleElement : rule.getBody())
                {
                    if (ruleElement instanceof Variable)
                    {
                        ruleFirst = tokenConcatter.concatPrefix(k, ruleFirst, first.get(ruleElement));
                    }
                    else if (ruleElement instanceof TokenSpec)
                    {
                        List<TokenSpec> tokenFirstList = new ArrayList<TokenSpec>();
                        tokenFirstList.add((TokenSpec) ruleElement);

                        Set<List<TokenSpec>> firstOfToken = new HashSet<List<TokenSpec>>();
                        firstOfToken.add(tokenFirstList);

                        ruleFirst = tokenConcatter.concatPrefix(k, ruleFirst, firstOfToken);
                    }
                }

                first.get(rule.getHead()).addAll(ruleFirst);
            }

            oldHash = newHash;
            newHash = first.hashCode();
        } while (oldHash != newHash);
    }

    public Set<Rule> getRulesContaining(RuleElement element)
    {
        Set<Rule> rules = new HashSet<Rule>();

        for (final Rule rule : this.rules)
        {
            if (rule.getBody().contains(element))
            {
                rules.add(rule);
            }
        }

        return rules;
    }

    public boolean isNullable(Variable variable)
    {
        return this.nullables.contains(variable);
    }

    public Set<TokenSpec> first(Rule rule)
    {
        return first(rule.getHead());
    }

    public Set<TokenSpec> first(Variable variable)
    {
        Set<TokenSpec> first = new HashSet<TokenSpec>();
        first(variable, first);
        return first;
    }

    private void first(Variable variable, Set<TokenSpec> firstSet)
    {
        first(variable, 0, firstSet);
    }

    private void first(Variable variable, int offset, Set<TokenSpec> firstSet)
    {
        for (final Rule rule : getRulesFor(variable))
        {
            RuleElement first = rule.getBody().get(offset);
            if (first instanceof TokenSpec)
            {
                firstSet.add((TokenSpec)first);
            }
            else if (first instanceof Variable && first != rule.getHead())
            {
                Variable var = (Variable)first;
                first(var, firstSet);
                if (isNullable(var))
                {
                    first(variable, offset + 1, firstSet);
                }
            }
        }
    }

    public Variable getStart()
    {
        return start;
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder(getClass().getSimpleName() + "(\n");

        for (final TokenSpec token : getTokens())
        {
            out.append('\t').append(token).append('\n');
        }

        for (final Rule rule : getRules())
        {
            out.append('\t').append(rule).append('\n');
        }
        return out.append(")").toString();
    }
}
