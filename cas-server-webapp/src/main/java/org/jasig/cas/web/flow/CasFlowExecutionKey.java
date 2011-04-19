/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.execution.repository.support.CompositeFlowExecutionKey;

import java.io.Serializable;
import java.util.UUID;

/**
 * Creates a flow execution ID of the following form:
 * <p>
 * <code>LT-{UUID}Ze{executionId}s{snapshotId}</code>
 * 
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.4.7
 */
public final class CasFlowExecutionKey extends CompositeFlowExecutionKey {

    /** Flow execution key string format. */
    public static final String FORMAT = "LT-{UUID}Ze{executionId}s{snapshotId}";

    /** Flow execution key prefix. */
    public final static String KEY_PREFIX = "LT-";

    /** Separates random UUID component from execution and snapshot IDs. */
    public final static String KEY_SEPARATOR = "Z";
    
    /** Serialization version marker. */
    private static final long serialVersionUID = -1535846700101524714L;

    private final UUID uuid;

    /**
     * Creates a new flow execution ID with the given parameters.
     *
     * @param executionId
     * @param snapshotId
     * @param uuid
     */
    public CasFlowExecutionKey(final Serializable executionId, final Serializable snapshotId, final UUID uuid) {
        super(executionId, snapshotId);
        this.uuid = uuid;
    }

    /**
     * Gets the random UUID component of the key.
     *
     * @return  Key UUID component.
     */
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(50);
        sb.append(KEY_PREFIX);
        sb.append(this.uuid);
        sb.append(KEY_SEPARATOR);
        sb.append(super.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.uuid != null ? this.uuid.hashCode() : 0);
        return result;
    }

    public static String[] keyParts(final String encodedKey) {
        final String[] keyParts = new String[3];
        final int keySeparatorIndex = encodedKey.indexOf(KEY_SEPARATOR);
        keyParts[0] = encodedKey.substring(KEY_PREFIX.length(), keySeparatorIndex);
        final String originalKey = encodedKey.substring(keySeparatorIndex+1);
        final String[] originalKeys = CompositeFlowExecutionKey.keyParts(originalKey);
        keyParts[1] = originalKeys[0];
        keyParts[2] = originalKeys[1];

        return keyParts;
    }
}
