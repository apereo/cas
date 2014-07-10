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
 * @since 4.1
 */
public final class SymmetricCipherEncoder implements ReversibleEncoder {

    /** Default cipher is AES. */
    public static final String DEFAULT_CIPHER = "AES";

    /** Symmetric cipher name. */
    @NotNull
    private String cipher = DEFAULT_CIPHER;

    /** Symmetric encryption/decryption key. */
    @NotNull
    private SecretKey key;

    /** Cipher initialization vector as hexadecimal string of bytes. */
    @NotNull
    private String hexIV;


    /**
     * Decrypts a base64-encoded ciphertext string of the serialized bytes
     * of an object and produces the original object through deserialization.
     *
     * @param encodedObject Base64-encoded ciphertext of serialized object.
     *
     * @return Deserialized object.
     */
    public Object decode(final String encodedObject) {
        final byte[] serialBytes;
        final SymmetricAlgorithm alg = createAlgorithm();
        try {
            alg.initDecrypt();
            serialBytes = alg.decrypt(encodedObject, new Base64Converter());
        } catch (final CryptException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new ByteArrayInputStream(serialBytes));
            return in.readObject();
        } catch (final Exception e) {
            throw new RuntimeException("Deserialization error", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


    /**
     * Produces the base64-encoded ciphertext of the serialized bytes of the
     * given object.
     *
     * @param object Object to encrypt.
     *
     * @return Base64-encoded ciphertext string.
     *
     */
    public String encode(final Serializable object) {
        final DirectByteArrayOutputStream outBytes = new DirectByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(outBytes);
            out.writeObject(object);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
        }

        return encode(outBytes.toByteArray());

    }


    /**
     * Produces the base64-encoded ciphertext of the given bytes.
     *
     * @param bytes Bytes to encrypt.
     *
     * @return Base64-encoded ciphertext string.
     *
     */
    public String encode(final byte[] bytes) {
        final SymmetricAlgorithm alg = createAlgorithm();
        try {
            alg.initEncrypt();
            return alg.encrypt(bytes, new Base64Converter());
        } catch (final CryptException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param cipher Name of symmetric cipher to use for encryption/decryption.
     * Default is AES.
     */
    public void setCipher(final String cipher) {
        this.cipher = cipher;
    }


    /**
     * @param key Symmetric encryption/decryption key.
     */
    public void setKey(final SecretKey key) {
        this.key = key;
    }

    /**
     * @param hexIV Cipher initialization vector bytes as a hexadecimal string.
     */
    public void setHexIV(final String hexIV) {
        this.hexIV = hexIV;
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
