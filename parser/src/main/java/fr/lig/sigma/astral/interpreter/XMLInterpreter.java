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

import fr.lig.sigma.astral.interpreter.common.XMLUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;

/**
 *
 */
@Component
@Provides
public class XMLInterpreter extends AbstractInterpreter {

    /*@Requires
    public XPathFactory xpathFact;

    @Requires
    public DocumentBuilderFactory dbf;
      */
    @Validate
    private void ready() {
        XMLUtils.setFactories(DocumentBuilderFactory.newInstance(), XPathFactory.newInstance());
    }

    @Override
    public Document parse(String query) throws ParsingException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(query.getBytes()));
            doc.normalizeDocument();
            return doc;
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }
}
