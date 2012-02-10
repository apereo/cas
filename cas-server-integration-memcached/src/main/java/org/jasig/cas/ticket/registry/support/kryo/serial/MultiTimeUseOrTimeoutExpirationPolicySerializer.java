/*
  $Id: $

  Copyright (C) 2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
*/
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;

import java.nio.ByteBuffer;

/**
 * Description of TimeoutExpirationPolicySerializer.
 *
 * @author Middleware Services
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
        buffer.putLong((Integer) fieldHelper.getFieldValue(policy, "numberOfUses"));
        buffer.putLong((Long) fieldHelper.getFieldValue(policy, "timeToKillInMilliSeconds"));
    }

    public MultiTimeUseOrTimeoutExpirationPolicy read(final ByteBuffer buffer) {
        return new MultiTimeUseOrTimeoutExpirationPolicy(buffer.getInt(), buffer.getLong());
    }
}
