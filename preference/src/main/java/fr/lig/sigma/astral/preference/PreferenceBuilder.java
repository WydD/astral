package fr.lig.sigma.astral.preference;

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.operators.relational.evaluate.EvaluateExpressionParseException;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.operators.relational.sigma.Condition;
import fr.lig.sigma.astral.query.AstralCore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class PreferenceBuilder {
    public final static HashSet<String> EMPTY_STRING_SET = new HashSet<String>();
    public final static Condition NULL_COND = new Condition() {
        public boolean evaluate(Tuple t) {
            return true;
        }

        public Set<String> getConcernedAttributes() {
            return EMPTY_STRING_SET;
        }
    };

    /**
     * Builds the partial comparator from the preference specification
     *
     * @param preference List of preference node as described in the xml
     * @param core       The core to get the Expression Analyzer
     * @return The partial comparator (a composite one)
     * @throws fr.lig.sigma.astral.operators.relational.evaluate.EvaluateExpressionParseException
     *          If weird expressions are in the specs
     * @throws fr.lig.sigma.astral.common.WrongAttributeException
     *          If there is an attribute inconsistency
     */
    public static PartialComparator<Tuple> buildProfile(List<Map<String, String>> preference, AstralCore core)
            throws EvaluateExpressionParseException, WrongAttributeException {
        core.getEs();
        CompositeConditionalPreference comparator = new CompositeConditionalPreference();
        ExpressionAnalyzer ea = core.getEngine().getGlobalEA();
        for (Map<String, String> pref : preference) {
            String condition = pref.get("condition");
            String preferred = pref.get("preferred");
            String over = pref.get("over");
            String ignored = pref.get("ignored");

            PartialComparator<Tuple> comp = new ConditionalPreferenceAtomic(
                    condition == null ? NULL_COND : ea.buildCondition(condition),
                    ea.buildCondition(preferred),
                    ea.buildCondition(over),
                    ignored == null ? EMPTY_STRING_SET : Arrays.asList(ignored.replaceAll(" ", "").split(",")));
            comparator.addComparator(comp);
        }
        return comparator;
    }
}
