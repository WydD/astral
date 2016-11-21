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

package fr.lig.sigma.astral.interpreter.common;

import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;

public class XMLManipulationUtil {
    /**
     * Extract the list of concerned attributes inside a node that has conditions
     *
     * @param e The node containing conditions (sigma/join)
     * @return A set o  f attributes
     */
    public static Set<String> extractConditionAttributes(Element e, ExpressionAnalyzer ea) {
        return ea.getArguments(e.getAttribute("condition"));
        //return extractConditionAttributes(e, "condition");
    }

    /**
     * Extract the list of concerned attributes inside a node that has conditions
     *
     * @param tagName Tag name of the condition (basically condition or aggregate)
     * @param e       The node containing conditions (sigma/join)
     * @return A set of attributes
     */
    public static Set<String> extractConditionAttributes(Element e, String tagName) {
        NodeList list = XMLUtils.list("./" + tagName + "/descendant-or-self::" + tagName, e);
        Set<String> conditionAttributes = new HashSet<String>();
        for (int i = 0; i < list.getLength(); i++) {
            Element condition = (Element) list.item(i);
            conditionAttributes.add(condition.getAttribute("attribute"));
            String other = condition.getAttribute("otherAttribute");
            if (other != null && !other.isEmpty()) conditionAttributes.add(other);
        }
        return conditionAttributes;
    }

    /**
     * Permutes pi with e
     *
     * @param e  the current element
     * @param in the child
     * @param pi the pi element
     */
    public static void swap(Element e, ExtendedElement<Set<String>> in, Element pi) {
        Element piContainer = (Element) pi.getParentNode();
        pi.replaceChild(in.getElt(), e);
        piContainer.replaceChild(e, pi);
        e.appendChild(pi);
    }

    /**
     * Insert a pi element after e over input
     *
     * @param e               the current element
     * @param pi              the pi element
     * @param input           the input
     * @param inputAttributes the attributes to set to pi
     * @return The create Pi element
     */
    public static Element insertPi(Element e, Element pi, ExtendedElement<Set<String>> input, Set<String> inputAttributes) {
        Element piClone = (Element) pi.cloneNode(false);
        piClone.appendChild(input.getElt());
        piClone.setAttribute("attributes", AttributeSet.string(inputAttributes));
        e.appendChild(piClone);
        return piClone;
    }

    /**
     * Remove the Pi on top of e
     *
     * @param e  the current element
     * @param pi the pi element
     */
    public static void removeTopPi(Element e, Element pi) {
        Element piContainer = (Element) pi.getParentNode();
        piContainer.replaceChild(e, pi);
    }

    public static void swapAndKeepAttributes(Element e, ExtendedElement<Set<String>> in, Set<String> conditionAttributes) {
        Set<String> topAttrib = in.getE();

        Element pi = (Element) e.getParentNode();
        if (topAttrib.containsAll(conditionAttributes)) {
            // Rule sigma/pi without restriction, pi/sigma = sigma/pi
            swap(e, in, pi);
        } else {
            // Rule sigma/pi with restriction, pi/sigma = pi/sigma/pi
            conditionAttributes.addAll(topAttrib);
            insertPi(e, pi, in, conditionAttributes);
            e.setAttribute("attributes", AttributeSet.string(conditionAttributes));
        }
        in.setE(conditionAttributes);
    }
}