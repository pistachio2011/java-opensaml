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

/**
 * 
 */

package org.opensaml.saml1.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AssertionIDReference;

/**
 * Test case for {@link org.opensaml.saml1.core.impl.AssertionIDReferenceImpl}
 */
public class AssertionIDReferenceTest extends BaseSAMLObjectProviderTestCase {

    private final String expectedNCName;

    /** name used to generate objects */
    private final QName qname;


    /**
     * Constructor
     */
    public AssertionIDReferenceTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAssertionIDReference.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleAssertionIDReferenceContents.xml";
        expectedNCName = "NibbleAHappyWarthog";
        qname = new QName(SAMLConstants.SAML1_NS, AssertionIDReference.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {
        AssertionIDReference assertionIDReference;

        assertionIDReference = (AssertionIDReference) unmarshallElement(singleElementFile);

        assertNull("NCName was " + assertionIDReference.getReference() + " expected null", assertionIDReference
                .getReference());
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesUnmarshall() {
        AssertionIDReference assertionIDReference;

        assertionIDReference = (AssertionIDReference) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("NCName ", expectedNCName, assertionIDReference.getReference());
    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesMarshall() {
        AssertionIDReference assertionIDReference = (AssertionIDReference) buildXMLObject(qname);

        assertionIDReference.setReference(expectedNCName);
        assertEquals(expectedOptionalAttributesDOM, assertionIDReference);
    }
}
