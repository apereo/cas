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
import java.util.Date;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;

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
