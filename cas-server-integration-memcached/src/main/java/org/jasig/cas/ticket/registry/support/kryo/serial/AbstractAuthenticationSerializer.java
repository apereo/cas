/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

/**
 * Serializer for classes that extend {@link org.jasig.cas.authentication.AbstractAuthentication}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public abstract class AbstractAuthenticationSerializer<T extends Authentication> extends SimpleSerializer<T> {

    private final Kryo kryo;

    private final AttributeMapSerializer attrSerializer;

    public AbstractAuthenticationSerializer(final Kryo kryo) {
        this.kryo = kryo;
        attrSerializer = new AttributeMapSerializer(kryo);
    }

    public void write(final ByteBuffer buffer, final T auth) {
        buffer.putLong(auth.getAuthenticatedDate().getTime());
        kryo.writeClassAndObject(buffer, auth.getPrincipal());
        attrSerializer.write(buffer, auth.getAttributes());
    }

    public T read(final ByteBuffer buffer) {
        return createAuthentication(
                new Date(buffer.getLong()),
                (Principal) kryo.readClassAndObject(buffer),
                attrSerializer.read(buffer));
    }

    protected abstract T createAuthentication(
            final Date authDate, final Principal p, final Map<String, Object> attributes);
}
