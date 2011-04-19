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

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Extension to the default Spring Web Flow {@link DefaultFlowExecutionRepository}
 * that supports {@link CasFlowExecutionKey}, which contains a random component
 * that is suitable for use as the LT parameter of the CAS protocol.  The random
 * UUID component of the flow key is stored as a conversation attribute to
 * support validation when the flow is resumed.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.4.7
 */
public final class CasFlowExecutionKeyFactory extends DefaultFlowExecutionRepository {

    private static final String UUID_KEY = "CAS_UUID";
    
    private final SecureRandom secureRandom = new SecureRandom();

    private boolean defaultBehavior = false;

    @NotNull
    private final ConversationManager conversationManager;

    public CasFlowExecutionKeyFactory(final ConversationManager conversationManager, final FlowExecutionSnapshotFactory snapshotFactory) {
        super(conversationManager, snapshotFactory);
        this.conversationManager = conversationManager;
    }

    /**
     * Set to true to enable the default Spring Webflow execution key format.
     * <p>
     * <strong>NOTE:</strong>
     * Setting to true is technically a violation of the CAS protocol since the
     * LT is required to have randomness that the default SWF key format lacks.
     *
     * @param defaultBehavior True to enable default behavior, false otherwise.
     * Default is false.
     */
    public void setDefaultBehavior(final boolean defaultBehavior) {
        this.defaultBehavior = defaultBehavior;
    }

    public FlowExecutionKey getKey(final FlowExecution execution) {
        if (this.defaultBehavior) {
            return super.getKey(execution);
        }

		CasFlowExecutionKey key = (CasFlowExecutionKey) execution.getKey();
		if (key == null) {
	        final Conversation conversation = this.conversationManager.beginConversation(
	            createConversationParameters(execution));
			final ConversationId executionId = conversation.getId();
            key = newKey(conversation, executionId);
		} else {
			if (getAlwaysGenerateNewNextKey()) {
                final Serializable executionId = key.getExecutionId();
                key = newKey(getConversation(executionId), executionId);
			}
		}
		return key;
    }

	public FlowExecutionKey parseFlowExecutionKey(final String encodedKey) throws FlowExecutionRepositoryException {
        if (this.defaultBehavior) {
            return super.parseFlowExecutionKey(encodedKey);
        }

		if (!StringUtils.hasText(encodedKey)) {
			throw new IllegalStateException("The string-encoded flow execution key is required");
		}
		final String[] keyParts = CasFlowExecutionKey.keyParts(encodedKey);
		final UUID uuid;
		try {
		    uuid = UUID.fromString(keyParts[0]);
		} catch (Exception e) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey, CasFlowExecutionKey.FORMAT);
		}
		Serializable executionId = parseExecutionId(keyParts[1], encodedKey);
		Serializable snapshotId = parseSnapshotId(keyParts[2], encodedKey);
		validateUUID(uuid, executionId);
		return new CasFlowExecutionKey(executionId, snapshotId, uuid);
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
	
	private CasFlowExecutionKey newKey(final Conversation conversation, final Serializable executionId) {
	    // Include time component in UUID to prevent successive dupes
	    // Use ~24h of millis so we have more bytes for random data
	    final long hi_word = (long) this.secureRandom.nextInt() << 32;
	    final long lo_word = System.currentTimeMillis() & 0xFFFFFFFF;
	    final UUID uuid = new UUID(hi_word | lo_word, this.secureRandom.nextLong());
	    conversation.lock();
	    try {
	        conversation.putAttribute(UUID_KEY, uuid);
	    } finally {
	        conversation.unlock();
	    }
	    return new CasFlowExecutionKey(executionId, nextSnapshotId(executionId), uuid);
	}
	
	private void validateUUID(final UUID givenUUID, final Serializable executionId) {
	    final Conversation conversation = getConversation(executionId);
	    conversation.lock();
	    try {
	        final UUID savedUUID = (UUID) conversation.getAttribute(UUID_KEY);
	        if (!givenUUID.equals(savedUUID)) {
	            throw new IllegalStateException("UUID component of flow execution key not recognized.");
	        }
	    } finally {
	        conversation.unlock();
	    }
	}

}
