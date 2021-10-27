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
package org.jasig.cas.extension.clearpass;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Decorator for a map that will hash the key and encrypt the value.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 1.0.6
 */
public final class EncryptedMapDecorator implements Map<String, String> {

	private static final String							CIPHER_ALGORITHM				= "AES/CBC/PKCS5Padding";

	private static final String							SECRET_KEY_FACTORY_ALGORITHM	= "PBKDF2WithHmacSHA1";

	private static final String							DEFAULT_HASH_ALGORITHM			= "SHA-512";

	private static final String							DEFAULT_ENCRYPTION_ALGORITHM	= "AES";

	private static final char[]							HEX_DIGITS						= { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'													};

	private final Log									log								= LogFactory.getLog(getClass());

	@NotNull
	private final Map<String, String>					decoratedMap;

	@NotNull
	private final MessageDigest							messageDigest;

	@NotNull
	private final byte[]								salt;

	@NotNull
	private final Key									key;

	@NotNull
	private final String								secretKeyAlgorithm;

	private boolean										cloneNotSupported;

	private ConcurrentHashMap<Object, IvParameterSpec>	algorithmParametersHashMap		= new ConcurrentHashMap<Object, IvParameterSpec>();

	/**
	 * Decorates a map using the default algorithm {@link #DEFAULT_HASH_ALGORITHM} and a {@link #DEFAULT_ENCRYPTION_ALGORITHM}.
	 * <p>The salt is randomly constructed when the object is created in memory.  This constructor is sufficient to decorate
	 * a cache that only lives in-memory.
	 *
	 * @param decoratedMap the map to decorate.  CANNOT be NULL.
	 * @throws NoSuchAlgorithmException if the algorithm cannot be found.  Should not happen in this case.
	 * @throws java.security.spec.InvalidKeySpecException if the key spec is not found.
	 * @throws java.security.InvalidKeyException if the key is invalid.
	 */
	public EncryptedMapDecorator(final Map<String, String> decoratedMap) throws Exception {
		this(decoratedMap, getRandomSalt(8), getRandomSalt(32));
	}

	/**
	 * Decorates a map using the default algorithm {@link #DEFAULT_HASH_ALGORITHM} and a {@link #DEFAULT_ENCRYPTION_ALGORITHM}.
	 * <p>Takes a salt and secretKey so that it can work with a distributed cache.
	 *
	 * @param decoratedMap the map to decorate.  CANNOT be NULL.
	 * @param salt the salt, as a String. Gets converted to bytes.   CANNOT be NULL.
	 * @param secretKey the secret to use for the key.  Gets converted to bytes.  CANNOT be NULL.
	 * @throws NoSuchAlgorithmException if the algorithm cannot be found.  Should not happen in this case.
	 * @throws java.security.spec.InvalidKeySpecException if the key spec is not found.
	 * @throws java.security.InvalidKeyException if the key is invalid.
	 */
	public EncryptedMapDecorator(final Map<String, String> decoratedMap, final String salt, final String secretKey) throws Exception {
		this(decoratedMap, DEFAULT_HASH_ALGORITHM, salt, DEFAULT_ENCRYPTION_ALGORITHM, secretKey);
	}

	/**
	 * Decorates a map using the provided algorithms.
	 * <p>Takes a salt and secretKey so that it can work with a distributed cache.
	 *
	 * @param decoratedMap the map to decorate.  CANNOT be NULL.
	 * @param hashAlgorithm the algorithm to use for hashing.  CANNOT BE NULL.
	 * @param salt the salt, as a String. Gets converted to bytes.   CANNOT be NULL.
	 * @param encryptionAlgorithm the encryption algorithm. CANNOT BE NULL.
	 * @param secretKey the secret to use for the key.  Gets converted to bytes.  CANNOT be NULL.
	 * @throws NoSuchAlgorithmException if the algorithm cannot be found.  Should not happen in this case.
	 * @throws java.security.spec.InvalidKeySpecException if the key spec is not found.
	 * @throws java.security.InvalidKeyException if the key is invalid.
	 */
	public EncryptedMapDecorator(final Map<String, String> decoratedMap, final String hashAlgorithm, final String salt,
			final String secretKeyAlgorithm, final String secretKey) throws Exception {
		this(decoratedMap, hashAlgorithm, salt.getBytes(), secretKeyAlgorithm, getSecretKey(secretKeyAlgorithm, secretKey, salt));
	}

	/**
	 * Decorates a map using the provided algorithms.
	 * <p>Takes a salt and secretKey so that it can work with a distributed cache.
	 *
	 * @param decoratedMap the map to decorate.  CANNOT be NULL.
	 * @param hashAlgorithm the algorithm to use for hashing.  CANNOT BE NULL.
	 * @param salt the salt, as a String. Gets converted to bytes.   CANNOT be NULL.
	 * @param encryptionAlgorithm the encryption algorithm. CANNOT BE NULL.
	 * @param secretKey the secret to use.  CANNOT be NULL.
	 * @throws NoSuchAlgorithmException if the algorithm cannot be found.  Should not happen in this case.
	 */
	public EncryptedMapDecorator(final Map<String, String> decoratedMap, final String hashAlgorithm, final byte[] salt,
			final String secretKeyAlgorithm, final Key secretKey) throws NoSuchAlgorithmException {
		this.decoratedMap = decoratedMap;
		this.key = secretKey;
		this.salt = salt;
		this.secretKeyAlgorithm = secretKeyAlgorithm;
		this.messageDigest = MessageDigest.getInstance(hashAlgorithm);
	}

	private static String getRandomSalt(final int size) {
		final SecureRandom secureRandom = new SecureRandom();
		final byte[] bytes = new byte[size];

		secureRandom.nextBytes(bytes);

		return getFormattedText(bytes);
	}

	public int size() {
		return this.decoratedMap.size();
	}

	public boolean isEmpty() {
		return this.decoratedMap.isEmpty();
	}

	public boolean containsKey(final Object key) {
		final String hashedKey = constructHashedKey(key.toString());
		return this.decoratedMap.containsKey(hashedKey);
	}

	public boolean containsValue(final Object value) {
		if (!(value instanceof String)) {
			return false;
		}

		final String encryptedValue = encrypt((String) value);
		return this.decoratedMap.containsValue(encryptedValue);
	}

	public String get(final Object key) {
		final String hashedKey = constructHashedKey(key == null ? null : key.toString());
		return decrypt(this.decoratedMap.get(hashedKey), hashedKey);
	}

	public String put(final String key, final String value) {
		final String hashedKey = constructHashedKey(key);
		final String hashedValue = encrypt(value, hashedKey);
		final String oldValue = this.decoratedMap.put(hashedKey, hashedValue);

		return decrypt(oldValue, hashedKey);
	}

	public String remove(final Object key) {
		final String hashedKey = constructHashedKey(key.toString());
		return decrypt(this.decoratedMap.remove(hashedKey), hashedKey);
	}

	public void putAll(final Map<? extends String, ? extends String> m) {
		for (final Entry<? extends String, ? extends String> entry : m.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	public void clear() {
		this.decoratedMap.clear();
	}

	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	public Collection<String> values() {
		throw new UnsupportedOperationException();
	}

	public Set<Entry<String, String>> entrySet() {
		throw new UnsupportedOperationException();
	}

	protected String constructHashedKey(final String key) {
		if (key == null) {
			return null;
		}

		final MessageDigest messageDigest = getMessageDigest();
		messageDigest.update(this.salt);
		messageDigest.update(key.getBytes());
		final String hash = getFormattedText(messageDigest.digest());

		if (log.isDebugEnabled()) {
			log.debug(String.format("Generated hash of value [%s] for key [%s].", hash, key));
		}
		return hash;
	}

	protected String decrypt(final String value, String hashedKey) {
		if (value == null)
			return null;

		try {
			final Cipher cipher = getCipherObject();

			byte[] ivByteArray = algorithmParametersHashMap.get(hashedKey).getIV();
			IvParameterSpec ivSpec = new IvParameterSpec(ivByteArray);

			cipher.init(Cipher.DECRYPT_MODE, this.key, ivSpec);

			byte[] valueByteArray = value.getBytes();
			byte[] decrypted64ByteValue = new Base64().decode(valueByteArray);
			byte[] decryptedByteArray = cipher.doFinal(decrypted64ByteValue);

			return new String(decryptedByteArray);

		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String encrypt(final String value) {
		return encrypt(value, null);
	}

	protected String encrypt(final String value, String hashedKey) {
		if (value == null)
			return null;

		try {
			final Cipher cipher = getCipherObject();
			cipher.init(Cipher.ENCRYPT_MODE, this.key);
			AlgorithmParameters params = cipher.getParameters();

			if (hashedKey != null)
				algorithmParametersHashMap.put(hashedKey, params.getParameterSpec(IvParameterSpec.class));

			byte[] valueByteArray = value.getBytes();
			byte[] encryptedByteArray = cipher.doFinal(valueByteArray);
			byte[] encrypted64ByteValue = new Base64().encode(encryptedByteArray);

			return new String(encrypted64ByteValue);

		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected static String byte2char(final byte[] bytes) throws UnsupportedEncodingException {
		return new String(bytes, "UTF-8");
	}

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
				throw new IllegalStateException("MessageDigest algorithm '" + algorithm + "' was supported when " + this.getClass().getSimpleName()
						+ " was created but is not now. This should not be possible.", e);
			}
		}

		try {
			return (MessageDigest) this.messageDigest.clone();
		} catch (final CloneNotSupportedException e) {
			this.cloneNotSupported = true;
			log.warn("Could not clone MessageDigest using algorithm '" + this.messageDigest.getAlgorithm()
					+ "'. MessageDigest.getInstance will be used from now on which will be much more expensive.", e);
			return this.getMessageDigest();
		}
	}

	/**
	* Takes the raw bytes from the digest and formats them correct.
	*
	* @param bytes the raw bytes from the digest.
	* @return the formatted bytes.
	*/
	private static String getFormattedText(final byte[] bytes) {
		final StringBuilder buf = new StringBuilder(bytes.length * 2);

		for (byte b : bytes) {
			buf.append(HEX_DIGITS[(b >> 4) & 0x0f]);
			buf.append(HEX_DIGITS[b & 0x0f]);
		}
		return buf.toString();
	}

	private Cipher getCipherObject() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return Cipher.getInstance(CIPHER_ALGORITHM);
	}

	private static Key getSecretKey(String secretKeyAlgorithm, String secretKey, String salt) throws Exception {

		SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
		KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), char2byte(salt), 65536, 128);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), secretKeyAlgorithm);

		return secret;
	}

	public String getSecretKeyAlgorithm() {
		return secretKeyAlgorithm;
	}
}
