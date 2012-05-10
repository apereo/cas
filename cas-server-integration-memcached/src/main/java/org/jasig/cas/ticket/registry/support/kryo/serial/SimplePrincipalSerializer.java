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
import org.jasig.cas.authentication.principal.SimplePrincipal;

/**
 * Serializer for {@link SimplePrincipal} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public final class SimplePrincipalSerializer extends SimpleSerializer<SimplePrincipal> {
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
