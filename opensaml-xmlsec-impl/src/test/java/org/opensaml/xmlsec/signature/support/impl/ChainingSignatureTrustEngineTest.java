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

package org.opensaml.xmlsec.signature.support.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class ChainingSignatureTrustEngineTest extends XMLObjectBaseTestCase {

    private CriteriaSet criteriaSet;

    private ChainingSignatureTrustEngine engine;

    private List<SignatureTrustEngine> chain;

    private Signature token;

    @BeforeMethod
    protected void setUp() throws Exception {

        token = buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);

        chain = new ArrayList<>();

        criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("dummyEntityID"));
    }

    @Test
    public void testFirstTrusted() throws SecurityException {
        chain.add(new MockSignatureTrustEngine(Boolean.TRUE));
        chain.add(new MockSignatureTrustEngine(Boolean.FALSE));
        engine = new ChainingSignatureTrustEngine(chain);
        Assert.assertTrue(engine.validate(token, criteriaSet), "Engine # 1 evaled token as trusted");
    }

    @Test
    public void testSecondTrusted() throws SecurityException {
        chain.add(new MockSignatureTrustEngine(Boolean.FALSE));
        chain.add(new MockSignatureTrustEngine(Boolean.TRUE));
        engine = new ChainingSignatureTrustEngine(chain);
        Assert.assertTrue(engine.validate(token, criteriaSet), "Engine # 2 evaled token as trusted");
    }

    @Test
    public void testNoneTrusted() throws SecurityException {
        chain.add(new MockSignatureTrustEngine(Boolean.FALSE));
        chain.add(new MockSignatureTrustEngine(Boolean.FALSE));
        engine = new ChainingSignatureTrustEngine(chain);
        Assert.assertFalse(engine.validate(token, criteriaSet), "No engine evaled token as trusted");
    }

    @Test
    public void testNullEngineOK() throws SecurityException {
        chain.add(new MockSignatureTrustEngine(Boolean.FALSE));
        chain.add(null);
        chain.add(new MockSignatureTrustEngine(Boolean.TRUE));
        engine = new ChainingSignatureTrustEngine(chain);
        Assert.assertTrue(engine.validate(token, criteriaSet),
                "Engine # 3 evaled token as trusted with intervening null engine");
    }

    @Test
    public void testRawSignature() throws SecurityException {
        chain.add(new MockSignatureTrustEngine(Boolean.FALSE));
        chain.add(null);
        chain.add(new MockSignatureTrustEngine(Boolean.TRUE));
        engine = new ChainingSignatureTrustEngine(chain);
        Assert.assertTrue(engine.validate(null, null, null, null, null),
                "Engine # 3 evaled raw signature as trusted with intervening null engine");
    }

    @Test(expectedExceptions = SecurityException.class)
    public void testException() throws SecurityException {
        chain.add(new MockSignatureTrustEngine(Boolean.FALSE));
        chain.add(new MockSignatureTrustEngine(null));
        engine = new ChainingSignatureTrustEngine(chain);
        engine.validate(token, criteriaSet);
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testNullChain() {
        engine = new ChainingSignatureTrustEngine(null);
    }

    /** Mock trust engine. */
    private class MockSignatureTrustEngine implements SignatureTrustEngine {

        private Boolean trusted;

        private MockSignatureTrustEngine(Boolean trusted) {
            this.trusted = trusted;
        }

        /** {@inheritDoc} */
        public boolean validate(Signature token, CriteriaSet trustBasisCriteria) throws SecurityException {
            if (trusted == null) {
                throw new SecurityException("This means an error happened");
            }
            return trusted;
        }

        /** {@inheritDoc} */
        public boolean validate(@Nonnull byte[] signature, @Nonnull byte[] content, @Nonnull String algorithmURI,
                @Nullable CriteriaSet trustBasisCriteria, @Nullable Credential candidateCredential)
                throws SecurityException {
            if (trusted == null) {
                throw new SecurityException("This means an error happened");
            }
            return trusted;
        }

        /** {@inheritDoc} */
        @Nullable public KeyInfoCredentialResolver getKeyInfoResolver() {
            return null;
        }

    }

}
