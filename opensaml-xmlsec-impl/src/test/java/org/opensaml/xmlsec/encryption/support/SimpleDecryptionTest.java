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

package org.opensaml.xmlsec.encryption.support;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import org.testng.Assert;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.SecretKey;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.XMLSecurityHelper;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.support.Decrypter;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.Encrypter;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.encryption.support.EncryptionParameters;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.mock.SignableSimpleXMLObject;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.w3c.dom.Document;

/**
 * Simple tests for decryption.
 */
public class SimpleDecryptionTest extends XMLObjectBaseTestCase {
    
    private KeyInfoCredentialResolver keyResolver;
    private KeyInfoCredentialResolver kekResolver;
    
    private String encURI;
    private Key encKey;
    private EncryptionParameters encParams;
    private EncryptedData encryptedData;
    private EncryptedData encryptedContent;
    
    private String kekURI;
    private KeyEncryptionParameters kekParams;
    private EncryptedKey encryptedKey;
    
    private String targetFile;
    private Document targetDOM;
    private SignableSimpleXMLObject targetObject;

    /**
     * Constructor.
     *
     */
    public SimpleDecryptionTest() {
        super();
        
        encURI = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
        kekURI = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;
        
        targetFile = "/data/org/opensaml/xmlsec/encryption/support/SimpleDecryptionTest.xml";
    }
    
    /** {@inheritDoc} */
    @BeforeMethod
    protected void setUp() throws Exception {
        Credential encCred = XMLSecurityHelper.generateKeyAndCredential(encURI);
        encKey = encCred.getSecretKey();
        keyResolver = new StaticKeyInfoCredentialResolver(encCred);
        encParams = new EncryptionParameters();
        encParams.setAlgorithm(encURI);
        encParams.setEncryptionCredential(encCred);
        
        
        Credential kekCred = XMLSecurityHelper.generateKeyPairAndCredential(kekURI, 1024, true);
        kekResolver = new StaticKeyInfoCredentialResolver(kekCred);
        kekParams = new KeyEncryptionParameters();
        kekParams.setAlgorithm(kekURI);
        kekParams.setEncryptionCredential(kekCred);
        
        Encrypter encrypter = new Encrypter();
        encryptedKey = encrypter.encryptKey(encKey, kekParams, parserPool.newDocument());
        
        
        targetDOM = parserPool.parse(SimpleDecryptionTest.class.getResourceAsStream(targetFile));
        targetObject = (SignableSimpleXMLObject) unmarshallElement(targetFile);
        try {
            encryptedData = encrypter.encryptElement(targetObject, encParams);
            encryptedContent = encrypter.encryptElementContent(targetObject, encParams);
        } catch (EncryptionException e) {
            Assert.fail("Object encryption failed: " + e);
        }
        
    }
    
    /**
     * Test simple decryption of an EncryptedKey object.
     */
    @Test
    public void testEncryptedKey() {
        Decrypter decrypter = new Decrypter(null, kekResolver, null);
       
        Key decryptedKey = null;
        try {
            decryptedKey = decrypter.decryptKey(encryptedKey, encURI);
        } catch (DecryptionException e) {
            Assert.fail("Error on decryption of EncryptedKey: " + e);
        }
        
        Assert.assertEquals(encKey, decryptedKey, "Decrypted EncryptedKey");
        
    }
    
    /**
     *  Test simple decryption of an EncryptedData object which is of type Element.
     */
    @Test
    public void testEncryptedElement() {
        Decrypter decrypter = new Decrypter(keyResolver, null, null);
        
        XMLObject decryptedXMLObject = null;
        try {
            decryptedXMLObject = decrypter.decryptData(encryptedData);
        } catch (DecryptionException e) {
            Assert.fail("Error on decryption of EncryptedData to element: " + e);
        }
        
        assertXMLEquals(targetDOM, decryptedXMLObject);
        
    }
    
    /**
     *  Test decryption of an EncryptedData object which is of type Element, where the decryption
     *  key is found as an inline EncryptedKey within EncryptedData/KeyInfo.
     */
    @Test
    public void testEncryptedElementWithEncryptedKeyInline() {
        KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        keyInfo.getEncryptedKeys().add(encryptedKey);
        encryptedData.setKeyInfo(keyInfo);
        
        EncryptedKeyResolver ekr = new InlineEncryptedKeyResolver();
        
        Decrypter decrypter = new Decrypter(null, kekResolver, ekr);
        
        XMLObject decryptedXMLObject = null;
        try {
            decryptedXMLObject = decrypter.decryptData(encryptedData);
        } catch (DecryptionException e) {
            Assert.fail("Error on decryption of EncryptedData to element: " + e);
        }
        
        assertXMLEquals(targetDOM, decryptedXMLObject);
        
    }
    
    /**
     *  Test error condition of no resolvers configured.
     */
    @Test
    public void testErrorNoResolvers() {
        Decrypter decrypter = new Decrypter(null, null, null);
        
        try {
            decrypter.decryptData(encryptedData);
            Assert.fail("Decryption should have failed, no resolvers configured");
        } catch (DecryptionException e) {
            // do nothing, should fail
        }
        
    }
    
    /**
     *  Test error condition of invalid data decryption key.
     *  
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testErrorInvalidDataDecryptionKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        Key badKey = XMLSecurityHelper.generateKeyFromURI(encURI);
        BasicCredential encCred = new BasicCredential();
        encCred.setSecretKey((SecretKey) badKey);
        KeyInfoCredentialResolver badEncResolver = new StaticKeyInfoCredentialResolver(encCred);
        
        Decrypter decrypter = new Decrypter(badEncResolver, null, null);
        
        try {
            decrypter.decryptData(encryptedData);
            Assert.fail("Decryption should have failed, invalid data decryption key");
        } catch (DecryptionException e) {
            // do nothing, should fail
        }
        
    }
    
    /**
     *  Test error condition of invalid key decryption key.
     *  
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testErrorInvalidKeyDecryptionKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair badKeyPair = XMLSecurityHelper.generateKeyPairFromURI(kekURI, 1024);
        BasicCredential kekCred = new BasicCredential();
        kekCred.setPublicKey(badKeyPair.getPublic());
        kekCred.setPrivateKey(badKeyPair.getPrivate());
        KeyInfoCredentialResolver badKEKResolver = new StaticKeyInfoCredentialResolver(kekCred);
        
        Decrypter decrypter = new Decrypter(null, badKEKResolver, null);
        
        try {
            decrypter.decryptKey(encryptedKey, encURI);
            Assert.fail("Decryption should have failed, invalid key decryption key");
        } catch (DecryptionException e) {
            // do nothing, should fail
        }
        
    }
    
    /**
     *  Test simple decryption of an EncryptedData object which is of type Content.
     */
    @Test
    public void testEncryptedContent() {
        Decrypter decrypter = new Decrypter(keyResolver, null, null);
        
        try {
            decrypter.decryptData(encryptedContent);
            Assert.fail("This should have failed, decryption of element content not yet supported");
        } catch (DecryptionException e) {
            //fail("Error on decryption of EncryptedData to element content: " + e);
            //Currently this will fail, not yet supporting decryption of element content.
            Assert.assertTrue(true, "Decryption of element content not yet supported");
        }
        
    }
 
}
