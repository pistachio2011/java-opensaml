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

package org.opensaml.saml.saml2.metadata;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.AttributeExtensibleXMLObject;
import org.opensaml.core.xml.ElementExtensibleXMLObject;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;

/**
 * SAML 2.0 Metadata Endpoint data type interface.
 */
public interface Endpoint extends SAMLObject, ElementExtensibleXMLObject, AttributeExtensibleXMLObject {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "Endpoint";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML20MD_NS, DEFAULT_ELEMENT_LOCAL_NAME,
            SAMLConstants.SAML20MD_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME = "EndpointType";

    /** QName of the XSI type. */
    public static final QName TYPE_NAME = new QName(SAMLConstants.SAML20MD_NS, TYPE_LOCAL_NAME,
            SAMLConstants.SAML20MD_PREFIX);

    /** "Binding" attribute name. */
    public static final String BINDING_ATTRIB_NAME = "Binding";

    /** "Location" attribute name. */
    public static final String LOCATION_ATTRIB_NAME = "Location";

    /** "ResponseLocation" attribute name. */
    public static final String RESPONSE_LOCATION_ATTRIB_NAME = "ResponseLocation";

    /**
     * Gets the URI identifier for the binding supported by this Endpoint.
     * 
     * @return the URI identifier for the binding supported by this Endpoint
     */
    public String getBinding();

    /**
     * Sets the URI identifier for the binding supported by this Endpoint.
     * 
     * @param binding the URI identifier for the binding supported by this Endpoint
     */
    public void setBinding(String binding);

    /**
     * Gets the URI, usually a URL, for the location of this Endpoint.
     * 
     * @return the location of this Endpoint
     */
    public String getLocation();

    /**
     * Sets the URI, usually a URL, for the location of this Endpoint.
     * 
     * @param location the location of this Endpoint
     */
    public void setLocation(String location);

    /**
     * Gets the URI, usually a URL, responses should be sent to this for this Endpoint.
     * 
     * @return the URI responses should be sent to this for this Endpoint
     */
    public String getResponseLocation();

    /**
     * Sets the URI, usually a URL, responses should be sent to this for this Endpoint.
     * 
     * @param location the URI responses should be sent to this for this Endpoint
     */
    public void setResponseLocation(String location);
}