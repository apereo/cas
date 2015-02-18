/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.extension.clearpass;

import org.jasig.cas.util.CompressionUtils;
import com.google.common.io.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Decorator for a map that will hash the key and encrypt the value.
 *
 * @author Scott Battaglia
 * @since 1.0.6
 */
public final class EncryptedMapDecorator implements Map<String, String> {

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA1";

    private static final String DEFAULT_HASH_ALGORITHM = "SHA-512";

    private static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES";

    private static final int INTEGER_LEN = 4;

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int DEFAULT_SALT_SIZE = 8;
    private static final int DEFAULT_SECRET_KEY_SIZE = 32;
    private static final int BYTE_BUFFER_CAPACITY_SIZE = 4;
    private static final int HEX_RIGHT_SHIFT_COEFFICIENT = 4;
    private static final int HEX_HIGH_BITS_BITWISE_FLAG = 0x0f;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    private final Map<String, String> decoratedMap;

    @NotNull
    private final MessageDigest messageDigest;

    @NotNull
    private final ByteSource salt;

    @NotNull
    private final Key key;

    @NotNull
    private int ivSize;

    @NotNull
    private final String secretKeyAlgorithm;

    private boolean cloneNotSupported;

    /**
     * Decorates a map using the default algorithm {@link #DEFAULT_HASH_ALGORITHM} and a
     * {@link #DEFAULT_ENCRYPTION_ALGORITHM}.
     * <p>The salt is randomly constructed when the object is created in memory.
     * This constructor is sufficient to decorate
     * a cache that only lives in-memory.
     *
     * @param decoratedMap the map to decorate.  CANNOT be NULL.
     * @throws Exception if the algorithm cannot be found.  Should not happen in this case, or if the key spec is not found
     * or if the key is invalid. Check the exception type for more details on the nature of the error.
     */
    public EncryptedMapDecorator(final Map<String, String> decoratedMap) throws Exception {
        this(decoratedMap, getRandomSalt(DEFAULT_SALT_SIZE), getRandomSalt(DEFAULT_SECRET_KEY_SIZE));
    }

    /**
     * Decorates a map using the default algorithm {@link #DEFAULT_HASH_ALGORITHM}
     * and a {@link #DEFAULT_ENCRYPTION_ALGORITHM}.
     * <p>Takes a salt and secretKey so that it can work with a distributed cache.
     *
     * @param decoratedMap the map to decorate.  CANNOT be NULL.
     * @param salt the salt, as a String. Gets converted to bytes.   CANNOT be NULL.
     * @param secretKey the secret to use for the key.  Gets converted to bytes.  CANNOT be NULL.
     * @throws Exception if the algorithm cannot be found.  Should not happen in this case, or if the key spec is not found
     * or if the key is invalid. Check the exception type for more details on the nature of the error.
     */
    public EncryptedMapDecorator(final Map<String, String> decoratedMap, final String salt,
            final String secretKey) throws Exception {
        this(decoratedMap, DEFAULT_HASH_ALGORITHM, salt, DEFAULT_ENCRYPTION_ALGORITHM, secretKey);
    }

    /**
     * Decorates a map using the provided algorithms.
     * <p>Takes a salt and secretKey so that it can work with a distributed cache.
     *
     * @param decoratedMap the map to decorate.  CANNOT be NULL.
     * @param hashAlgorithm the algorithm to use for hashing.  CANNOT BE NULL.
     * @param salt the salt, as a String. Gets converted to bytes.   CANNOT be NULL.
     * @param secretKeyAlgorithm the encryption algorithm. CANNOT BE NULL.
     * @param secretKey the secret to use for the key.  Gets converted to bytes.  CANNOT be NULL.
     * @throws Exception if the algorithm cannot be found.  Should not happen in this case, or if the key spec is not found
     * or if the key is invalid. Check the exception type for more details on the nature of the error.
     */
    public EncryptedMapDecorator(final Map<String, String> decoratedMap, final String hashAlgorithm, final String salt,
            final String secretKeyAlgorithm, final String secretKey) throws Exception {
        this(decoratedMap, hashAlgorithm, salt.getBytes(Charset.defaultCharset()), secretKeyAlgorithm,
                getSecretKey(secretKeyAlgorithm, secretKey, salt));
    }

    /**
     * Decorates a map using the provided algorithms.
     * <p>Takes a salt and secretKey so that it can work with a distributed cache.
     *
     * @param decoratedMap the map to decorate.  CANNOT be NULL.
     * @param hashAlgorithm the algorithm to use for hashing.  CANNOT BE NULL.
     * @param salt the salt, as a String. Gets converted to bytes.   CANNOT be NULL.
     * @param secretKeyAlgorithm the encryption algorithm. CANNOT BE NULL.
     * @param secretKey the secret to use.  CANNOT be NULL.
     * @throws RuntimeException if the algorithm cannot be found or the iv size cant be determined.
     */
    public EncryptedMapDecorator(final Map<String, String> decoratedMap, final String hashAlgorithm, final byte[] salt,
            final String secretKeyAlgorithm, final Key secretKey) {
        try {
            this.decoratedMap = decoratedMap;
            this.key = secretKey;
            this.salt = ByteSource.wrap(salt);
            this.secretKeyAlgorithm = secretKeyAlgorithm;
            this.messageDigest = MessageDigest.getInstance(hashAlgorithm);
            this.ivSize = getIvSize();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the random salt.
     *
     * @param size the size
     * @return the random salt
     */
    private static String getRandomSalt(final int size) {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] bytes = new byte[size];

        secureRandom.nextBytes(bytes);

        return getFormattedText(bytes);
    }

    @Override
    public int size() {
        return this.decoratedMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.decoratedMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        final String hashedKey = constructHashedKey(key.toString());
        return this.decoratedMap.containsKey(hashedKey);
    }

    @Override
    public boolean containsValue(final Object value) {
        if (!(value instanceof String)) {
            return false;
        }

        final String encryptedValue = encrypt((String) value);
        return this.decoratedMap.containsValue(encryptedValue);
    }

    @Override
    public String get(final Object key) {
        final String hashedKey = constructHashedKey(key == null ? null : key.toString());
        return decrypt(this.decoratedMap.get(hashedKey), hashedKey);
    }

    @Override
    public String put(final String key, final String value) {
        final String hashedKey = constructHashedKey(key);
        final String hashedValue = encrypt(value, hashedKey);
        final String oldValue = this.decoratedMap.put(hashedKey, hashedValue);

        return decrypt(oldValue, hashedKey);
    }

    @Override
    public String remove(final Object key) {
        final String hashedKey = constructHashedKey(key.toString());
        return decrypt(this.decoratedMap.remove(hashedKey), hashedKey);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends String> m) {
        for (final Entry<? extends String, ? extends String> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.decoratedMap.clear();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Construct hashed key.
     *
     * @param key the key
     * @return the string
     */
    protected String constructHashedKey(final String key) {
        if (key == null) {
            return null;
        }

        final MessageDigest messageDigest = getMessageDigest();
        messageDigest.update(consumeByteSourceOrNull(this.salt));
        messageDigest.update(key.toLowerCase().getBytes(Charset.defaultCharset()));
        final String hash = getFormattedText(messageDigest.digest());

        logger.debug("Generated hash of value [{}] for key [{}].", hash, key);
        return hash;
    }

    /**
     * Decrypt the value.
     *
     * @param value the value
     * @param hashedKey the hashed key
     * @return the string
     */
    protected String decrypt(final String value, final String hashedKey) {
        if (value == null) {
            return null;
        }

        try {
            final Cipher cipher = getCipherObject();
            final byte[] ivCiphertext = CompressionUtils.decodeBase64ToByteArray(value);
            final int ivSize = byte2int(Arrays.copyOfRange(ivCiphertext, 0, INTEGER_LEN));
            final byte[] ivValue = Arrays.copyOfRange(ivCiphertext, INTEGER_LEN, (INTEGER_LEN + ivSize));
            final byte[] ciphertext = Arrays.copyOfRange(ivCiphertext, INTEGER_LEN + ivSize, ivCiphertext.length);
            final IvParameterSpec ivSpec = new IvParameterSpec(ivValue);
            
            cipher.init(Cipher.DECRYPT_MODE, this.key, ivSpec);

            final byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, Charset.defaultCharset());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the contents of the source into a byte array.
     * @param source  the byte array source
     * @return the byte[] read from the source or null
     */
    private byte[] consumeByteSourceOrNull(final ByteSource source) {
        try {
            if (source == null || source.isEmpty()) {
                return null;
            }
            return source.read();
        } catch (final IOException e) {
            logger.warn("Could not consume the byte array source", e);
            return null;
        }
    }

    /**
     * Gets the iv size.
     *
     * @return the iv size
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws NoSuchPaddingException the no such padding exception
     */
    private int getIvSize() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return getCipherObject().getBlockSize();
    }

    /**
     * Generate iv.
     *
     * @param size the size
     * @return the iv value
     */
    private static byte[] generateIV(final int size) {
        final SecureRandom srand = new SecureRandom();
        final byte[] ivValue = new byte[size];
        srand.nextBytes(ivValue);
        return ivValue;
    }


    /**
     * Encrypt.
     *
     * @param value the value
     * @return the string
     */
    protected String encrypt(final String value) {
        return encrypt(value, null);
    }

    /**
     * Encrypt.
     *
     * @param value the value
     * @param hashedKey the hashed key
     * @return the string
     */
    protected String encrypt(final String value, final String hashedKey) {
        if (value == null) {
            return null;
        }

        try {
            final Cipher cipher = getCipherObject();
            final byte[] ivValue = generateIV(this.ivSize);
            final IvParameterSpec ivSpec = new IvParameterSpec(ivValue);

            cipher.init(Cipher.ENCRYPT_MODE, this.key, ivSpec);
            
            final byte[] ciphertext = cipher.doFinal(value.getBytes(Charset.defaultCharset()));
            final byte[] ivCiphertext = new byte[INTEGER_LEN + this.ivSize + ciphertext.length];

            System.arraycopy(int2byte(this.ivSize), 0, ivCiphertext, 0, INTEGER_LEN);
            System.arraycopy(ivValue, 0, ivCiphertext, INTEGER_LEN, this.ivSize);
            System.arraycopy(ciphertext, 0, ivCiphertext, INTEGER_LEN + this.ivSize, ciphertext.length);

            return CompressionUtils.encodeBase64(ivCiphertext);
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Int to byte.
     *
     * @param i the i
     * @return the byte[]
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    protected static byte[] int2byte(final int i) throws UnsupportedEncodingException {
        return ByteBuffer.allocate(BYTE_BUFFER_CAPACITY_SIZE).putInt(i).array();
    }

    /**
     * Byte to int.
     *
     * @param bytes the bytes
     * @return the int
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    protected static int byte2int(final byte[] bytes) throws UnsupportedEncodingException {
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * Byte to char.
     *
     * @param bytes the bytes
     * @return the string
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    protected static String byte2char(final byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "UTF-8");
    }

    /**
     * Char to byte.
     *
     * @param chars the chars
     * @return the byte[]
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    protected static byte[] char2byte(final String chars) throws UnsupportedEncodingException {
        return chars.getBytes("UTF-8");
    }

    /**
     * Tries to clone the {@link MessageDigest} that was created during construction. If the clone fails
     * that is remembered and from that point on new {@link MessageDigest} instances will be created on
     * every call.
     * <p>
     * Adopted from the Spring EhCache Annotations project.
     *
     * @return Generates a {@link MessageDigest} to use
     */
    protected MessageDigest getMessageDigest() {
        if (this.cloneNotSupported) {
            final String algorithm = this.messageDigest.getAlgorithm();
            try {
                return MessageDigest.getInstance(algorithm);
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalStateException("MessageDigest algorithm '" + algorithm + "' was supported when "
                        + this.getClass().getSimpleName()
                        + " was created but is not now. This should not be possible.", e);
            }
        }

        try {
            return (MessageDigest) this.messageDigest.clone();
        } catch (final CloneNotSupportedException e) {
            this.cloneNotSupported = true;
            final String msg = String.format("Could not clone MessageDigest using algorithm '%s'. "
                        + "MessageDigest.getInstance will be used from now on which will be much more expensive.",
                        this.messageDigest.getAlgorithm());
            logger.warn(msg, e);
            return this.getMessageDigest();
        }
    }

    /**
     * Takes the raw bytes from the digest and formats them.
     *
     * @param bytes the raw bytes from the digest.
     * @return the formatted bytes.
     */
    private static String getFormattedText(final byte[] bytes) {
        final StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            buf.append(HEX_DIGITS[b >> HEX_RIGHT_SHIFT_COEFFICIENT & HEX_HIGH_BITS_BITWISE_FLAG]);
            buf.append(HEX_DIGITS[b & HEX_HIGH_BITS_BITWISE_FLAG]);
        }
        return buf.toString();
    }

    /**
     * Gets the cipher object for the {@link #CIPHER_ALGORITHM}.
     *
     * @return the cipher object
     * @throws NoSuchAlgorithmException - if transformation is null, empty, in an invalid format, or if no Provider
     * supports a CipherSpi implementation for the specified algorithm. 
     * @throws NoSuchPaddingException - if transformation contains a padding scheme that is not available.
     * @see Cipher#getInstance(String)
     */
    private Cipher getCipherObject() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(CIPHER_ALGORITHM);
    }

    /**
     * Gets the secret key.
     *
     * @param secretKeyAlgorithm the secret key algorithm
     * @param secretKey the secret key
     * @param salt the salt
     * @return the secret key
     * @throws Exception the exception
     */
    private static Key getSecretKey(final String secretKeyAlgorithm, final String secretKey,
            final String salt) throws Exception {

        final SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
        final KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), char2byte(salt), 65536, 128);
        final SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), secretKeyAlgorithm);
    }

    public String getSecretKeyAlgorithm() {
        return secretKeyAlgorithm;
    }
}
