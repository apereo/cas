/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import java.nio.ByteBuffer;

/**
 * Serializer for classes that extend {@link org.jasig.cas.authentication.principal.AbstractWebApplicationService}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public abstract class AbstractWebApplicationServiceSerializer<T extends AbstractWebApplicationService>
        extends SimpleSerializer<T> {

    protected final Kryo kryo;
    
    protected final FieldHelper fieldHelper;

    public AbstractWebApplicationServiceSerializer(final Kryo kryo, final FieldHelper helper) {
        this.kryo = kryo;
        this.fieldHelper = helper;
    }

    public void write(final ByteBuffer buffer, final T service) {
        kryo.writeObjectData(buffer, service.getId());
        kryo.writeObject(buffer, fieldHelper.getFieldValue(service, "originalUrl"));
        kryo.writeObject(buffer, service.getArtifactId());
    }

    public T read(final ByteBuffer buffer) {
        return createService(
                buffer,
                kryo.readObjectData(buffer, String.class),
                kryo.readObject(buffer, String.class),
                kryo.readObject(buffer, String.class));
    }

    protected abstract T createService(
            final ByteBuffer buffer, final String id, final String originalUrl, final String artifactId);
}
