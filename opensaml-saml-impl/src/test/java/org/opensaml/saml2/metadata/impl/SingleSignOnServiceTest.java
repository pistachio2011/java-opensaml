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
package org.opensaml.saml2.metadata.impl;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.org;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml.saml2.metadata.impl.SingleLogoutServiceImpl}.
 */
public class SingleSignOnServiceTest extends BaseSAMLObjectProviderTestCase {
    
    protected String expectedBinding;
    protected String expectedLocation;
    protected String expectedResponseLocation;
    
    /**
     * Constructor
     */
    public SingleSignOnServiceTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/SingleSignOnService.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/metadata/impl/SingleSignOnServiceOptionalAttributes.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedBinding = "urn:binding:foo";
        expectedLocation = "example.org";
        expectedResponseLocation = "example.org/response";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SingleSignOnService service = ( SingleSignOnService) unmarshallElement(singleElementFile);
        
        assertEquals("Binding URI was not expected value", expectedBinding, service.getBinding());
        assertEquals("Location was not expected value", expectedLocation, service.getLocation());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        SingleSignOnService service = ( SingleSignOnService) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("Binding URI was not expected value", expectedBinding, service.getBinding());
        assertEquals("Location was not expected value", expectedLocation, service.getLocation());
        assertEquals("ResponseLocation was not expected value", expectedResponseLocation, service.getResponseLocation());;
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        SingleSignOnService service = ( SingleSignOnService) buildXMLObject(SingleSignOnService.DEFAULT_ELEMENT_NAME);
        
        service.setBinding(expectedBinding);
        service.setLocation(expectedLocation);

        assertEquals(expectedDOM, service);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        SingleSignOnService service = ( SingleSignOnService) buildXMLObject(SingleSignOnService.DEFAULT_ELEMENT_NAME);
        
        service.setBinding(expectedBinding);
        service.setLocation(expectedLocation);
        service.setResponseLocation(expectedResponseLocation);

        assertEquals(expectedOptionalAttributesDOM, service);
    }
}