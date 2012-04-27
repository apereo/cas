/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;

/**
 * Serializer for {@link MultiTimeUseOrTimeoutExpirationPolicy}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class MultiTimeUseOrTimeoutExpirationPolicySerializer extends SimpleSerializer<MultiTimeUseOrTimeoutExpirationPolicy> {

    protected final FieldHelper fieldHelper;

    public MultiTimeUseOrTimeoutExpirationPolicySerializer(final FieldHelper helper) {
        this.fieldHelper = helper;
    }

    public void write(final ByteBuffer buffer, final MultiTimeUseOrTimeoutExpirationPolicy policy) {
        buffer.putInt((Integer) fieldHelper.getFieldValue(policy, "numberOfUses"));
        buffer.putLong((Long) fieldHelper.getFieldValue(policy, "timeToKillInMilliSeconds"));
    }

    public MultiTimeUseOrTimeoutExpirationPolicy read(final ByteBuffer buffer) {
        return new MultiTimeUseOrTimeoutExpirationPolicy(buffer.getInt(), buffer.getLong());
    }
}
