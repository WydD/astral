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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class XMLUtils {
    private static XPath xpath;
    private static DocumentBuilderFactory dbf;
    private static XPathFactory xpathFact;

    public static void setFactories(DocumentBuilderFactory dbf, XPathFactory xpathFact) {
        XMLUtils.dbf = dbf;
        XMLUtils.xpathFact = xpathFact;
        xpath = xpathFact.newXPath();
    }

    public static NodeList list(String path, Node e) {
        return (NodeList) evaluateXPath(path, e, XPathConstants.NODESET);
    }

    private static Map<String, XPathExpression> compiledString = new HashMap<String, XPathExpression>();

    private static Object evaluateXPath(String path, Node e, QName type) {
        try {
            XPathExpression expr = compiledString.get(path);
            if (expr == null) {
                expr = xpath.compile(path);
                compiledString.put(path, expr);
            }
            return expr.evaluate(e, type);
        } catch (XPathExpressionException e1) {
            throw new IllegalStateException("Wrong XPATH expression: " + path, e1);
            //SHOULD NEVER HAPPEN IF THE EXPRESSIONS ARE WELL MADE INSIDE THE CODE
        }
    }

    public static Node seek(String path, Node e) {
        return (Node) evaluateXPath(path, e, XPathConstants.NODE);
    }

    public static String get(String path, Node e) {
        return (String) evaluateXPath(path, e, XPathConstants.STRING);
    }

    public static String xmlToString(Node doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
