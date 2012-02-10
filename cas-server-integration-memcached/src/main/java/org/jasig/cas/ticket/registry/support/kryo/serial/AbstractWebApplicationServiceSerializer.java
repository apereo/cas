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
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import java.nio.ByteBuffer;

/**
 * Description of AbstractWebApplicationServiceSerializer.
 *
 * @author Middleware Services
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
        kryo.writeObjectData(buffer, fieldHelper.getFieldValue(service, "originalUrl"));
        kryo.writeObjectData(buffer, service.getArtifactId());
    }

    public T read(final ByteBuffer buffer) {
        return createService(
                buffer,
                kryo.readObjectData(buffer, String.class),
                kryo.readObjectData(buffer, String.class),
                kryo.readObjectData(buffer, String.class));
    }

    protected abstract T createService(
            final ByteBuffer buffer, final String id, final String originalUrl, final String artifactId);
}
