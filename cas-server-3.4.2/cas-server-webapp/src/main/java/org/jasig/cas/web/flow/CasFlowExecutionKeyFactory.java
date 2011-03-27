/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.springframework.util.StringUtils;
import org.springframework.webflow.conversation.*;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;
import org.springframework.webflow.execution.repository.impl.DefaultFlowExecutionRepository;
import org.springframework.webflow.execution.repository.snapshot.FlowExecutionSnapshotFactory;
import org.springframework.webflow.execution.repository.support.CompositeFlowExecutionKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

/**
 * Extension to the default Spring Web Flow {@link DefaultFlowExecutionRepository}.  We override a number of protected methods to
 * provide our own key which includes a random part. Due to the structure of the super class, we've also had to copy a
 * number of private methods to the sub class in order to have it actually work.  <strong>This is not a good idea.</strong>
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4.7
 */
public final class CasFlowExecutionKeyFactory extends DefaultFlowExecutionRepository {

    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES";

    public static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private boolean defaultBehavior = false;

    @NotNull
    private final ConversationManager conversationManager;

    @NotNull
    private final Key key;

    @NotNull
    private final String cipherAlgorithm;

    private final byte[] initialVector = getRandomSalt(16);

    private final IvParameterSpec ivs = new IvParameterSpec(this.initialVector);

    public CasFlowExecutionKeyFactory(final ConversationManager conversationManager, final FlowExecutionSnapshotFactory snapshotFactory) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        this(conversationManager, snapshotFactory, DEFAULT_CIPHER_ALGORITHM, KeyGenerator.getInstance(DEFAULT_ENCRYPTION_ALGORITHM).generateKey());
    }

    public CasFlowExecutionKeyFactory(final ConversationManager conversationManager, final FlowExecutionSnapshotFactory snapshotFactory, final String cipherAlgorithm, final Key secretKey) {
        super(conversationManager, snapshotFactory);
        this.conversationManager = conversationManager;
        this.key = secretKey;
        this.cipherAlgorithm = cipherAlgorithm;
    }

    private static byte[] getRandomSalt(final int size) {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] bytes = new byte[size];

        secureRandom.nextBytes(bytes);
        return bytes;
    }

    protected String decrypt(final String value) {
        if (value == null) {
            return null;
        }

        try {
            final Cipher cipher = Cipher.getInstance(this.cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, this.key, this.ivs);
            return new String(cipher.doFinal(hexStringToByteArray(value)));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String encrypt(final String value) {
        if (value == null) {
            return null;
        }

        try {
            final Cipher cipher = Cipher.getInstance(this.cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, this.key, this.ivs);
            return byteArrayToHexString(cipher.doFinal(value.getBytes()));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

public static String byteArrayToHexString(byte[] b){
     StringBuffer sb = new StringBuffer(b.length * 2);
     for (int i = 0; i < b.length; i++){
       int v = b[i] & 0xff;
       if (v < 16) {
         sb.append('0');
       }
       sb.append(Integer.toHexString(v));
     }
     return sb.toString().toUpperCase();
  }

    protected static byte[] hexStringToByteArray(final String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++){
          int index = i * 2;
          int v = Integer.parseInt(s.substring(index, index + 2), 16);
          b[i] = (byte)v;
        }
        return b;
    }

    public FlowExecutionKey getKey(final FlowExecution execution) {
        if (this.defaultBehavior) {
            return super.getKey(execution);
        }

		final CasFlowExecutionKey key = (CasFlowExecutionKey) execution.getKey();
		if (key == null) {
			final Conversation conversation = beginConversation(execution);
			final ConversationId executionId = conversation.getId();
            final Serializable nextSnapshotId = nextSnapshotId(executionId);
            final String unencryptedVersion = UUID.randomUUID().toString() + CasFlowExecutionKey.KEY_SEPARATOR + "e" + executionId + "s" + nextSnapshotId;
            final String encryptedVersion = encrypt(unencryptedVersion);
			return new CasFlowExecutionKey(executionId, nextSnapshotId, encryptedVersion);
		} else {
			if (getAlwaysGenerateNewNextKey()) {
                final Serializable executionId = key.getExecutionId();
                final Serializable snapshotId = nextSnapshotId(key.getExecutionId());
                final String unencryptedVersion = UUID.randomUUID().toString() + CasFlowExecutionKey.KEY_SEPARATOR + "e" + executionId + "s" + snapshotId;
                final String encryptedVersion = encrypt(unencryptedVersion);

				return new CasFlowExecutionKey(executionId, snapshotId, encryptedVersion);
			} else {
				return execution.getKey();
			}
		}
    }

	public FlowExecutionKey parseFlowExecutionKey(final String encodedKey) throws FlowExecutionRepositoryException {
        if (this.defaultBehavior) {
            return super.parseFlowExecutionKey(encodedKey);
        }


		if (!StringUtils.hasText(encodedKey)) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey, "The string-encoded flow execution key is required");
		}
        final String unencryptedVersion = decrypt(encodedKey);
		String[] keyParts = CasFlowExecutionKey.keyParts(unencryptedVersion);
		Serializable executionId = parseExecutionId(keyParts[0], encodedKey);
		Serializable snapshotId = parseSnapshotId(keyParts[1], encodedKey);
        return new CasFlowExecutionKey(executionId, snapshotId, encodedKey);
	}

	private ConversationId parseExecutionId(final String encodedId, final String encodedKey) throws BadlyFormattedFlowExecutionKeyException {
		try {
			return this.conversationManager.parseConversationId(encodedId);
		} catch (ConversationException e) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey, CompositeFlowExecutionKey.getFormat(), e);
		}
	}

	private Serializable parseSnapshotId(final String encodedId, final String encodedKey) throws BadlyFormattedFlowExecutionKeyException {
		try {
			return Integer.valueOf(encodedId);
		} catch (NumberFormatException e) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey, CompositeFlowExecutionKey.getFormat(), e);
		}
	}

    /**
     * Copied from super-class since its marked as private.
     * @param execution
     * @return
     */
    	private Conversation beginConversation(FlowExecution execution) {
		ConversationParameters parameters = createConversationParameters(execution);
		Conversation conversation = this.conversationManager.beginConversation(parameters);
		return conversation;
	}

    public void setDefaultBehavior(final boolean defaultBehavior) {
        this.defaultBehavior = defaultBehavior;
    }
}
