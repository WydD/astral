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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class BottomUpGuide<E> extends AbstractGuide<E,E,Element> {
    public BottomUpGuide(QueryVisitor<E,E> visitor) {
        super(visitor);
    }

    @SuppressWarnings({"unchecked"})
    public E doVisit(Element e) throws Exception {
        if (e == null) return null; // Declare only
        NodeList list = XMLUtils.list("*[@type or local-name() = 'query']", e);
        // very silly but there is no other solutions
        List<E> inputs = new LinkedList<E>();
        for (int i = 0; i < list.getLength(); i++) {
            inputs.add(doVisit((Element) list.item(i)));
        }
        return performVisit(e, inputs);
    }
}
