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

package org.opensaml.xmlsec.signature.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.xmlsec.signature.Exponent;
import org.opensaml.xmlsec.signature.Modulus;
import org.opensaml.xmlsec.signature.RSAKeyValue;

/**
 * Concrete implementation of {@link org.opensaml.xmlsec.signature.RSAKeyValue}.
 */
public class RSAKeyValueImpl extends AbstractXMLObject implements RSAKeyValue {
    
    /** Modulus child element value. */
    private Modulus modulus;
    
    /** Exponent child element value. */
    private Exponent exponent;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected RSAKeyValueImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public Modulus getModulus() {
        return this.modulus;
    }

    /** {@inheritDoc} */
    public void setModulus(Modulus newModulus) {
        this.modulus = prepareForAssignment(this.modulus, newModulus);
    }

    /** {@inheritDoc} */
    public Exponent getExponent() {
        return this.exponent;
    }

    /** {@inheritDoc} */
    public void setExponent(Exponent newExponent) {
        this.exponent = prepareForAssignment(this.exponent, newExponent);

    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<>();
        
        if (modulus != null) {
            children.add(modulus);
        }
        if (exponent != null) {
            children.add(exponent);
        }
        
        if (children.size() == 0) {
            return null;
        }
        
        return Collections.unmodifiableList(children);
    }

}