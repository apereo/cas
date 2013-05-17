/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

/**
 * Serializer for classes that extend {@link org.jasig.cas.authentication.principal.AbstractWebApplicationService}.
 *
 * @author Marvin S. Addison
 */
public abstract class AbstractWebApplicationServiceSerializer<T extends AbstractWebApplicationService>
        extends SimpleSerializer<T> {
    /** Kryo instance. **/
    protected final Kryo kryo;
    /** FieldHelper instance. **/
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
