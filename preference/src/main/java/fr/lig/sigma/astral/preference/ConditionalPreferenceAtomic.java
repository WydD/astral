/*
 * Copyright 2012 LIG SIGMA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.lig.sigma.astral.preference;

import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.common.types.ValueComparator;
import fr.lig.sigma.astral.operators.relational.sigma.Condition;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class ConditionalPreferenceAtomic implements PartialComparator<Tuple> {

    private Condition condition;
    private Condition preferred;
    private Condition over;
    private Collection<String> ignoreAttributes;
    private Set<String> equalAttributes;
    private static final Logger log = Logger.getLogger(ConditionalPreferenceAtomic.class);

    public ConditionalPreferenceAtomic(Condition condition, Condition preferred, Condition over, Collection<String> ignoreAttributes) throws WrongAttributeException {
        if (!preferred.getConcernedAttributes().equals(over.getConcernedAttributes()))
            throw new WrongAttributeException("The two side of the preference expression are not on the same attributes");
        Set<String> disjoin = new AttributeSet(condition.getConcernedAttributes());
        disjoin.retainAll(preferred.getConcernedAttributes());
        if (!disjoin.isEmpty())
            throw new WrongAttributeException("The condition has some attributes shared with the preference");
        disjoin = new AttributeSet(condition.getConcernedAttributes());
        disjoin.addAll(preferred.getConcernedAttributes());
        disjoin.retainAll(ignoreAttributes);
        if (!disjoin.isEmpty())
            throw new WrongAttributeException("The ignore list shares some attributes with the other components");
        this.condition = condition;
        this.preferred = preferred;
        this.over = over;
        this.ignoreAttributes = ignoreAttributes;
    }

    public void setContent(Set<String> s) {
        equalAttributes = new AttributeSet(s);
        equalAttributes.removeAll(ignoreAttributes);
        equalAttributes.removeAll(preferred.getConcernedAttributes());
        equalAttributes.remove(Tuple.PHYSICAL_ID);
    }

    @SuppressWarnings({"unchecked"})
    public Integer compare(Tuple e1, Tuple e2) {
        if (!condition.evaluate(e1))
            return null; // condition is not verified, not comparable

        for (String attr : equalAttributes) {
            if (ValueComparator.compare(e1.get(attr), e2.get(attr), 1) != 0)
                return null; // Not comparable
        }

        // Now we compare between preferred and over
        if (preferred.evaluate(e1) && over.evaluate(e2)) return 1;
        if (preferred.evaluate(e2) && over.evaluate(e1)) return -1;
        return null;
    }
}
