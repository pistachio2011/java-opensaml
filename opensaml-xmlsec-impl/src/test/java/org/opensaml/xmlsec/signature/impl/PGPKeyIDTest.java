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


import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import org.opensaml.core.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xmlsec.signature.PGPKeyID;

/**
 *
 */
public class PGPKeyIDTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedStringContent;

    /**
     * Constructor
     *
     */
    public PGPKeyIDTest() {
        singleElementFile = "/org/opensaml/xmlsec/signature/impl/PGPKeyID.xml";
        
    }

    @BeforeMethod
    protected void setUp() throws Exception {
        expectedStringContent = "somePGPKeyID";
    }

    /** {@inheritDoc} */
    @Test
    public void testSingleElementUnmarshall() {
        PGPKeyID pgpElement = (PGPKeyID) unmarshallElement(singleElementFile);
        
        Assert.assertNotNull(pgpElement, "PGPKeyID");
        Assert.assertEquals(expectedStringContent, pgpElement.getValue(), "PGPKeyID value");
    }

    /** {@inheritDoc} */
    @Test
    public void testSingleElementMarshall() {
        PGPKeyID pgpElement = (PGPKeyID) buildXMLObject(PGPKeyID.DEFAULT_ELEMENT_NAME);
        pgpElement.setValue(expectedStringContent);
        
        assertXMLEquals(expectedDOM, pgpElement);
    }

}
