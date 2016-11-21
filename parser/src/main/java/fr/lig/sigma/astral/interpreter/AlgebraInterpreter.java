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

package fr.lig.sigma.astral.interpreter;

import fr.lig.sigma.astral.interpreter.lexer.Lexer;
import fr.lig.sigma.astral.interpreter.parser.Parser;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class AlgebraInterpreter extends AbstractInterpreter {

    public AlgebraInterpreter() throws Exception {
        super();
    }

    public Document parse(String query) throws ParsingException {
        long t = System.currentTimeMillis();
        Lexer l = new Lexer(new StringReader(query));
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.setXmlVersion("1.0");
            doc.setXmlStandalone(true);
            Parser parser = new Parser(l, doc);
            Element node = (Element) parser.parse().value;
            node.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            node.setAttribute("xsi:noNamespaceSchemaLocation", "http://astral.ligforge.imag.fr/schema/query.xsd");
            doc.appendChild(node);
            log.trace((System.currentTimeMillis() - t) + "ms to parse");
            return doc;
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }

}
