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

package org.opensaml.security.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.collection.LazyMap;

import org.opensaml.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.io.PrivateKeyCredentialReader;

/**
 * Helper methods for cryptographic keys and key pairs.
 */
public final class KeySupport {

    /** Maps key algorithms to the signing algorithm used in the key matching function. */
    private static Map<String, String> keyMatchAlgorithms;

    /** Constructor. */
    private KeySupport() {
    }

    /**
     * Get the key length in bits of the specified key.
     * 
     * @param key the key to evaluate
     * @return length of the key in bits, or null if the length can not be determined
     */
    public static Integer getKeyLength(Key key) {
        Logger log = getLogger();
        // TODO investigate techniques (and use cases) to determine length in other cases,
        // e.g. RSA and DSA keys, and non-RAW format symmetric keys
        if (key instanceof SecretKey && "RAW".equals(key.getFormat())) {
            return key.getEncoded().length * 8;
        }
        log.debug("Unable to determine length in bits of specified Key instance");
        return null;
    }

    /**
     * Decodes secret keys in DER and PEM format.
     * 
     * This method is not yet implemented.
     * 
     * @param key secret key
     * @param password password if the key is encrypted or null if not
     * 
     * @return the decoded key
     * 
     * @throws KeyException thrown if the key can not be decoded
     */
    public static SecretKey decodeSecretKey(byte[] key, char[] password) throws KeyException {
        // TODO
        throw new UnsupportedOperationException("This method is not yet supported");
    }

    /**
     * Decodes RSA/DSA public keys in DER-encoded "SubjectPublicKeyInfo" format.
     * 
     * @param key encoded key
     * @param password password if the key is encrypted or null if not
     * 
     * @return decoded key
     * 
     * @throws KeyException thrown if the key can not be decoded
     */
    public static PublicKey decodePublicKey(byte[] key, char[] password) throws KeyException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        try {
            return buildKey(keySpec, "RSA");
        } catch (KeyException ex) {
        }
        try {
            return buildKey(keySpec, "DSA");
        } catch (KeyException ex) {
        }
        try {
            return buildKey(keySpec, "EC");
        } catch (KeyException ex) {
        }
        throw new KeyException("Unsupported key type.");
    }

    /**
     * Decodes RSA/DSA private keys in DER, PEM, or PKCS#8 (encrypted or unencrypted) formats.
     * 
     * @param key encoded key
     * @param password decryption password or null if the key is not encrypted
     * 
     * @return decoded private key
     * 
     * @throws KeyException thrown if the key can not be decoded
     */
    public static PrivateKey decodePrivateKey(File key, char[] password) throws KeyException {
        if (!key.exists()) {
            throw new KeyException("Key file " + key.getAbsolutePath() + " does not exist");
        }

        if (!key.canRead()) {
            throw new KeyException("Key file " + key.getAbsolutePath() + " is not readable");
        }

        try {
            return decodePrivateKey(Files.toByteArray(key), password);
        } catch (IOException e) {
            throw new KeyException("Error reading Key file " + key.getAbsolutePath(), e);
        }
    }

    /**
     * Decodes RSA/DSA private keys in DER, PEM, or PKCS#8 (encrypted or unencrypted) formats.
     * 
     * @param key encoded key
     * @param password decryption password or null if the key is not encrypted
     * 
     * @return deocded private key
     * 
     * @throws KeyException thrown if the key can not be decoded
     */
    public static PrivateKey decodePrivateKey(byte[] key, char[] password) throws KeyException {
            PrivateKeyCredentialReader credReader = new PrivateKeyCredentialReader();
            ByteArrayInputStream bais = new ByteArrayInputStream(key);
            try {
                if (password != null && password.length > 0) {
                    return credReader.read(bais, password);
                } else {
                    return credReader.read(bais);
                } 
            } catch (IOException e) {
                throw new KeyException("Unable to decode private key", e);
            } catch (CryptException e) {
                throw new KeyException("Unable to decode private key", e);
            }
    }
    
    /**
     * Derives the public key from either a DSA or RSA private key.
     * 
     * @param key the private key to derive the public key from
     * 
     * @return the derived public key
     * 
     * @throws KeyException thrown if the given private key is not a DSA or RSA key or there is a problem generating the
     *             public key
     */
    public static PublicKey derivePublicKey(PrivateKey key) throws KeyException {
        KeyFactory factory;
        if (key instanceof DSAPrivateKey) {
            DSAPrivateKey dsaKey = (DSAPrivateKey) key;
            DSAParams keyParams = dsaKey.getParams();
            BigInteger y = keyParams.getQ().modPow(dsaKey.getX(), keyParams.getP());
            DSAPublicKeySpec pubKeySpec = new DSAPublicKeySpec(y, keyParams.getP(), keyParams.getQ(), keyParams.getG());

            try {
                factory = KeyFactory.getInstance("DSA");
                return factory.generatePublic(pubKeySpec);
            } catch (GeneralSecurityException e) {
                throw new KeyException("Unable to derive public key from DSA private key", e);
            }
        } else if (key instanceof RSAPrivateCrtKey) {
            RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) key;
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());

            try {
                factory = KeyFactory.getInstance("RSA");
                return factory.generatePublic(pubKeySpec);
            } catch (GeneralSecurityException e) {
                throw new KeyException("Unable to derive public key from RSA private key", e);
            }
        } else {
            throw new KeyException("Private key was not a DSA or RSA key");
        }
    }

    /**
     * Build Java DSA public key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded DSA public key
     * @return a native Java DSAPublicKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static DSAPublicKey buildJavaDSAPublicKey(String base64EncodedKey) throws KeyException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64Support.decode(base64EncodedKey));
        return (DSAPublicKey) buildKey(keySpec, "DSA");
    }

    /**
     * Build Java RSA public key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded RSA public key
     * @return a native Java RSAPublicKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static RSAPublicKey buildJavaRSAPublicKey(String base64EncodedKey) throws KeyException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64Support.decode(base64EncodedKey));
        return (RSAPublicKey) buildKey(keySpec, "RSA");
    }

    /**
     * Build Java EC public key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded EC public key
     * @return a native Java ECPublicKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static ECPublicKey buildJavaECPublicKey(String base64EncodedKey) throws KeyException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64Support.decode(base64EncodedKey));
        return (ECPublicKey) buildKey(keySpec, "EC");
    }

    /**
     * Build Java RSA private key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded RSA private key
     * @return a native Java RSAPrivateKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static RSAPrivateKey buildJavaRSAPrivateKey(String base64EncodedKey) throws KeyException {
        PrivateKey key = buildJavaPrivateKey(base64EncodedKey);
        if (!(key instanceof RSAPrivateKey)) {
            throw new KeyException("Generated key was not an RSAPrivateKey instance");
        }
        return (RSAPrivateKey) key;
    }

    /**
     * Build Java DSA private key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded DSA private key
     * @return a native Java DSAPrivateKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static DSAPrivateKey buildJavaDSAPrivateKey(String base64EncodedKey) throws KeyException {
        PrivateKey key = buildJavaPrivateKey(base64EncodedKey);
        if (!(key instanceof DSAPrivateKey)) {
            throw new KeyException("Generated key was not a DSAPrivateKey instance");
        }
        return (DSAPrivateKey) key;
    }

    /**
     * Build Java private key from base64 encoding. The key should have no password.
     * 
     * @param base64EncodedKey base64-encoded private key
     * @return a native Java PrivateKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static PrivateKey buildJavaPrivateKey(String base64EncodedKey) throws KeyException {
        return decodePrivateKey(Base64Support.decode(base64EncodedKey), null);
    }

    /**
     * Generates a public key from the given key spec.
     * 
     * @param keySpec {@link KeySpec} specification for the key
     * @param keyAlgorithm key generation algorithm, only DSA and RSA supported
     * 
     * @return the generated {@link PublicKey}
     * 
     * @throws KeyException thrown if the key algorithm is not supported by the JCE or the key spec does not contain
     *             valid information
     */
    public static PublicKey buildKey(KeySpec keySpec, String keyAlgorithm) throws KeyException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException(keyAlgorithm + "algorithm is not supported by the JCE", e);
        } catch (InvalidKeySpecException e) {
            throw new KeyException("Invalid key information", e);
        }
    }

    /**
     * Generate a random symmetric key.
     * 
     * @param algo key algorithm
     * @param keyLength key length
     * @param provider JCA provider
     * @return randomly generated symmetric key
     * @throws NoSuchAlgorithmException algorithm not found
     * @throws NoSuchProviderException provider not found
     */
    public static SecretKey generateKey(String algo, int keyLength, String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        SecretKey key = null;
        KeyGenerator keyGenerator = null;
        if (provider != null) {
            keyGenerator = KeyGenerator.getInstance(algo, provider);
        } else {
            keyGenerator = KeyGenerator.getInstance(algo);
        }
        keyGenerator.init(keyLength);
        key = keyGenerator.generateKey();
        return key;
    }

    /**
     * Generate a random asymmetric key pair.
     * 
     * @param algo key algorithm
     * @param keyLength key length
     * @param provider JCA provider
     * @return randomly generated key
     * @throws NoSuchAlgorithmException algorithm not found
     * @throws NoSuchProviderException provider not found
     */
    public static KeyPair generateKeyPair(String algo, int keyLength, String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        KeyPairGenerator keyGenerator = null;
        if (provider != null) {
            keyGenerator = KeyPairGenerator.getInstance(algo, provider);
        } else {
            keyGenerator = KeyPairGenerator.getInstance(algo);
        }
        keyGenerator.initialize(keyLength);
        return keyGenerator.generateKeyPair();
    }

    /**
     * Compare the supplied public and private keys, and determine if they correspond to the same key pair.
     * 
     * @param pubKey the public key
     * @param privKey the private key
     * @return true if the public and private are from the same key pair, false if not
     * @throws SecurityException if the keys can not be evaluated, or if the key algorithm is unsupported or unknown
     */
    public static boolean matchKeyPair(PublicKey pubKey, PrivateKey privKey) throws SecurityException {
        Logger log = getLogger();
        // This approach attempts to match the keys by signing and then validating some known data.

        if (pubKey == null || privKey == null) {
            throw new SecurityException("Either public or private key was null");
        }

        String jcaAlgoID = keyMatchAlgorithms.get(privKey.getAlgorithm());
        if (jcaAlgoID == null) {
            throw new SecurityException("Can't determine JCA algorithm ID for key matching from key algorithm: "
                    + privKey.getAlgorithm());
        }

        if (log.isDebugEnabled()) {
            log.debug("Attempting to match key pair containing key algorithms public '{}' private '{}', "
                    + "using JCA signature algorithm '{}'", new Object[] {pubKey.getAlgorithm(),
                    privKey.getAlgorithm(), jcaAlgoID,});
        }

        byte[] data = "This is the data to sign".getBytes();
        byte[] signature = SigningUtil.sign(privKey, jcaAlgoID, data);
        return SigningUtil.verify(pubKey, jcaAlgoID, signature, data);
    }

    /**
     * Get an SLF4J Logger.
     * 
     * @return a Logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(KeySupport.class);
    }

    static {
        keyMatchAlgorithms = new LazyMap<String, String>();
        keyMatchAlgorithms.put("RSA", "SHA1withRSA");
        keyMatchAlgorithms.put("DSA", "SHA1withDSA");
        keyMatchAlgorithms.put("ECDSA", "SHA1withECDSA");
    }

}