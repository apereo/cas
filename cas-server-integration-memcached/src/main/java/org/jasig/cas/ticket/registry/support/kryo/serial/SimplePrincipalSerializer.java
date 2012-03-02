/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import java.nio.ByteBuffer;

/**
 * Serializer for {@link SimplePrincipal} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class SimplePrincipalSerializer extends SimpleSerializer<SimplePrincipal> {
    private final Kryo kryo;

    private final AttributeMapSerializer attrSerializer;

    public SimplePrincipalSerializer(final Kryo kryo) {
        this.kryo = kryo;
        attrSerializer = new AttributeMapSerializer(kryo);
    }

    public void write(final ByteBuffer buffer, final SimplePrincipal principal) {
        kryo.writeObjectData(buffer, principal.getId());
        attrSerializer.write(buffer, principal.getAttributes());
    }

    public SimplePrincipal read(final ByteBuffer buffer) {
        return new SimplePrincipal(
                kryo.readObjectData(buffer, String.class),
                attrSerializer.read(buffer));
    }
}
