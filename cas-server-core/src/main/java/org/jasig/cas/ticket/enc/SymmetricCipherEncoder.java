/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.ticket.enc;

import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.io.DirectByteArrayOutputStream;
import edu.vt.middleware.crypt.symmetric.SymmetricAlgorithm;
import edu.vt.middleware.crypt.util.Base64Converter;
import edu.vt.middleware.crypt.util.HexConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
/**
 * A reversible encoder that used symmetric encryption to encode and decode
 * serializable objects.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class SymmetricCipherEncoder implements ReversibleEncoder {

    /** Default cipher is AES. */
    public static final String DEFAULT_CIPHER = "AES";

    private static final int IV_LENGTH = 32;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Symmetric cipher name. */
    @NotNull
    private final String cipher;

    /** Symmetric encryption/decryption key. */
    @NotNull
    private SecretKey key;

    /** Cipher initialization vector as hexadecimal string of bytes. */
    @NotNull
    private final String hexIV;

    /**
     * Instantiates a new Symmetric cipher encoder
     * with the cipher {@link #DEFAULT_CIPHER},
     * a random 16-character hex iv and the given key.
     *
     * @param key the secret key
     */
    public SymmetricCipherEncoder(final SecretKey key) {
        this(DEFAULT_CIPHER, key, RandomStringUtils.randomAlphabetic(IV_LENGTH));
    }

    /**
     * Instantiates a new Symmetric cipher encoder.
     *
     * @param cipher the cipher
     * @param key the key
     * @param hexIV the hex iV
     */
    public SymmetricCipherEncoder(final String cipher, final SecretKey key, final String hexIV) {
        this.cipher = cipher;
        this.key = key;
        this.hexIV = hexIV;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object decode(final String encodedObject) {
        ObjectInputStream in = null;
        ByteArrayInputStream inStream = null;
        try {
            final SymmetricAlgorithm alg = createAlgorithm();

            alg.initDecrypt();
            final byte[] serialBytes = alg.decrypt(encodedObject, new Base64Converter());

            inStream = new ByteArrayInputStream(serialBytes);
            in = new ObjectInputStream(inStream);

            return in.readObject();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(inStream);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Serializable> T decode(final String encodedObject, final Class<? extends Serializable> clazz) {
        final Serializable obj = (T) decode(encodedObject);

        if (obj == null) {
            throw new RuntimeException("Can not decode encoded object " + encodedObject);
        }

        if (!clazz.isAssignableFrom(obj.getClass())) {
            throw new ClassCastException("Decoded object is of type " + obj.getClass()
                    + " when we were expecting " + clazz);
        }

        return (T) obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(final Serializable object) {
        DirectByteArrayOutputStream outBytes = null;
        ObjectOutputStream out = null;
        try {
            outBytes = new DirectByteArrayOutputStream();
            out = new ObjectOutputStream(outBytes);

            out.writeObject(object);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(outBytes);
        }

        return encode(outBytes.toByteArray());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(final byte[] bytes) {
        try {
            final SymmetricAlgorithm alg = createAlgorithm();
            alg.initEncrypt();
            return alg.encrypt(bytes, new Base64Converter());
        } catch (final CryptException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Creates a new instance of the cipher that performs encryption/decryption.
     *
     * @return New instance that needs to be initialized for either encryption
     * or decryption prior to use.
     */
    private SymmetricAlgorithm createAlgorithm() {
        final SymmetricAlgorithm alg = SymmetricAlgorithm.newInstance(this.cipher);
        alg.setIV(new HexConverter().toBytes(this.hexIV));
        alg.setKey(this.key);
        return alg;
    }
}
