/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.util.HttpClient;

import java.nio.ByteBuffer;

/**
 * Serializer for {@link SimpleWebApplicationServiceImpl} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class SimpleWebApplicationServiceSerializer extends SimpleSerializer<SimpleWebApplicationServiceImpl> {
    protected final Kryo kryo;

    public SimpleWebApplicationServiceSerializer(final Kryo kryo) {
        this.kryo = kryo;
    }

    public void write(final ByteBuffer buffer, final SimpleWebApplicationServiceImpl service) {
        kryo.writeObjectData(buffer, service.getId());
    }

    public SimpleWebApplicationServiceImpl read(final ByteBuffer buffer) {
        return new SimpleWebApplicationServiceImpl(kryo.readObjectData(buffer, String.class), new HttpClient());
    }
}
