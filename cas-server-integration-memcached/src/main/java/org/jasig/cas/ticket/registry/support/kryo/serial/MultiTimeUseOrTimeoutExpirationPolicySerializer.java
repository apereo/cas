/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;

import java.nio.ByteBuffer;

/**
 * Serializer for {@link MultiTimeUseOrTimeoutExpirationPolicy}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class MultiTimeUseOrTimeoutExpirationPolicySerializer extends SimpleSerializer<MultiTimeUseOrTimeoutExpirationPolicy> {

    private final Kryo kryo;

    protected final FieldHelper fieldHelper;

    public MultiTimeUseOrTimeoutExpirationPolicySerializer(final Kryo kryo, final FieldHelper helper) {
        this.kryo = kryo;
        this.fieldHelper = helper;
    }

    public void write(final ByteBuffer buffer, MultiTimeUseOrTimeoutExpirationPolicy policy) {
        buffer.putInt((Integer) fieldHelper.getFieldValue(policy, "numberOfUses"));
        buffer.putLong((Long) fieldHelper.getFieldValue(policy, "timeToKillInMilliSeconds"));
    }

    public MultiTimeUseOrTimeoutExpirationPolicy read(final ByteBuffer buffer) {
        return new MultiTimeUseOrTimeoutExpirationPolicy(buffer.getInt(), buffer.getLong());
    }
}
