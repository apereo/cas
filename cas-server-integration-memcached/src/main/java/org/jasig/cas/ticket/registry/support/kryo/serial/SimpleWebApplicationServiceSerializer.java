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
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;

/**
 * Serializer for {@link SimpleWebApplicationServiceImpl} class.
 *
 * @author Marvin S. Addison
 */
public final class SimpleWebApplicationServiceSerializer extends SimpleSerializer<SimpleWebApplicationServiceImpl> {
    /** Kyro instance. **/
    protected final Kryo kryo;

    public SimpleWebApplicationServiceSerializer(final Kryo kryo) {
        this.kryo = kryo;
    }

    public void write(final ByteBuffer buffer, final SimpleWebApplicationServiceImpl service) {
        kryo.writeObjectData(buffer, service.getId());
    }

    public SimpleWebApplicationServiceImpl read(final ByteBuffer buffer) {
        return new SimpleWebApplicationServiceImpl(kryo.readObjectData(buffer, String.class));
    }
}
