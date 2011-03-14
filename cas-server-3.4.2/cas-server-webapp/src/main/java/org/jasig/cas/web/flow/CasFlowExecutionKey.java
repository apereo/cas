/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.execution.repository.support.CompositeFlowExecutionKey;

import java.io.Serializable;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4.7
 */
public final class CasFlowExecutionKey extends CompositeFlowExecutionKey {

    public final static String KEY_SEPARATOR = "Z";

    private final String encryptedValue;

    public CasFlowExecutionKey(final Serializable executionId, final Serializable snapshotId, final String encryptedVersion) {
        super(executionId, snapshotId);
        this.encryptedValue = encryptedVersion;
    }

    @Override
    public String toString() {
        return this.encryptedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CasFlowExecutionKey that = (CasFlowExecutionKey) o;

        if (encryptedValue != null ? !encryptedValue.equals(that.encryptedValue) : that.encryptedValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (encryptedValue != null ? encryptedValue.hashCode() : 0);
        return result;
    }

    public static String[] keyParts(final String encodedKey) {
        final String[] keyParts = new String[3];
        final int keySeparatorIndex = encodedKey.indexOf(KEY_SEPARATOR);
        keyParts[2] = encodedKey.substring(0, keySeparatorIndex);

        final String originalKey = encodedKey.substring(keySeparatorIndex+1);

        final String[] originalKeys = CompositeFlowExecutionKey.keyParts(originalKey);
        keyParts[0] = originalKeys[0];
        keyParts[1] = originalKeys[1];

        return keyParts;
    }
}
