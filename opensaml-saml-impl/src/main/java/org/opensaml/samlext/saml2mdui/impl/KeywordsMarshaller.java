/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.samlext.saml2mdui.impl;

import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.utilities.java.support.xml.XmlConstants;

import org.opensaml.core.xml.LangBearing;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.samlext.saml2mdui.Keywords;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.samlext.saml2mdui.Keywords} objects.
 */
public class KeywordsMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * {@inheritDoc}
     */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        Keywords words = (Keywords) samlObject;

        if (words.getXMLLang() != null) {
            Attr attribute = AttributeSupport.constructAttribute(domElement.getOwnerDocument(), XmlConstants.XML_NS,
                    LangBearing.XML_LANG_ATTR_LOCAL_NAME, XmlConstants.XML_PREFIX);
            attribute.setValue(words.getXMLLang());
            domElement.setAttributeNodeNS(attribute);
        }
    }

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) throws MarshallingException {
        Keywords words = (Keywords) samlObject;

        if (words.getKeywords() != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : words.getKeywords()) {
                sb.append(s);
                sb.append(' ');
            }
            ElementSupport.appendTextContent(domElement, sb.toString());
        }
    }
}