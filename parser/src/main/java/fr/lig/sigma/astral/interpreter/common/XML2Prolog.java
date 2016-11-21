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

import fr.lig.sigma.astral.interpreter.common.model.AbstractModel;
import fr.lig.sigma.astral.interpreter.common.model.EntityModel;
import fr.lig.sigma.astral.interpreter.common.model.OperatorModel;
import fr.lig.sigma.astral.interpreter.common.model.ParameterModel;
import fr.lig.sigma.astral.query.KnowledgeBase;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author Loic Petit
 */
public class XML2Prolog {
    private static final Logger log = Logger.getLogger(XML2Prolog.class);
    private static int key = 0;
    private KnowledgeBase kb;

    public static BufferedReader fromString(String s) {
        return new BufferedReader(new StringReader(s));
    }

    public XML2Prolog(KnowledgeBase kb) {
        this.kb = kb;
    }

    public String getProlog(Document dom) {
        try {
            Element n = (Element) XMLUtils.seek("//query/*[1]", dom.getDocumentElement());
            if (n == null) return "";
            AbstractModel prolog = getProlog(n);
            return prolog.toString();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public AbstractModel getProlog(Element n) {
        AbstractModel result = null;

        NodeList nl = n.getChildNodes();
        List<AbstractModel> childs = new LinkedList<AbstractModel>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if(!(item instanceof Element)) continue;
            Element child = (Element) item;
            AbstractModel model = getProlog(child);
            if (model instanceof EntityModel) {
                if (result == null)
                    result = new OperatorModel(n.getTagName());
                ((OperatorModel) result).getChilds().add(model);
            } else
                childs.add(model);
        }
        if (result == null)
            result = isSource(n) ? new EntityModel(n.getTagName()) : new ParameterModel(n.getTagName());

        NamedNodeMap attributeList = n.getAttributes();
        Map<String, Object> parameters = result.getParameters();
        for (int i = 0; i < attributeList.getLength(); i++) {
            Attr attribute = (Attr) attributeList.item(i);
            parameters.put(attribute.getName(), "\"" + attribute.getValue() + "\"");
        }

        for (AbstractModel model : childs) {
            String name = model.getName();
            List<Object> param = (List<Object>) parameters.get(name);
            if (param == null) {
                param = new LinkedList<Object>();
                parameters.put(name, param);
            }
            if (model instanceof ParameterModel) {
                param.add(model.getParameters());
            } else {
                param.add(model);
            }
        }
        return result;
    }

    private boolean isSource(Element n) {
        boolean source = n.getTagName().equals("source");
        if(!source) {
            try {
                source = kb.consult(":- solve(macrosource("+n.getTagName()+")).") != null;
            } catch (Exception e) {
                log.error("Weird error when knowing if the element "+n.getTagName()+" was a source", e);
            }
        }
        return source;
    }
}
