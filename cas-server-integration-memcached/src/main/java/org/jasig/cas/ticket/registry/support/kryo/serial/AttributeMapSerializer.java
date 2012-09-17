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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.RegisteredClass;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;

/**
 * Serializes the attribute map of a {@link org.jasig.cas.authentication.principal.SimplePrincipal}.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public final class AttributeMapSerializer extends SimpleSerializer<Map<String, Object>> {

    private final Kryo kryo;

    public AttributeMapSerializer(final Kryo kryo) {
        this.kryo = kryo;
    }

    public void write(final ByteBuffer buffer, final Map<String, Object> map) {
        buffer.putInt(map.size());
        Object value;
        Collection items;
        for (final String key : map.keySet()) {
            kryo.writeObjectData(buffer, key);
            value = map.get(key);
            if (value instanceof Collection) {
                items = (Collection) value;
                kryo.writeClass(buffer, ArrayList.class);
                buffer.putInt(items.size());
                for (final Object o : items) {
                    kryo.writeClassAndObject(buffer, o);
                }
            } else {
                kryo.writeClassAndObject(buffer, map.get(key));
            }
        }
    }

    public Map<String, Object> read(final ByteBuffer buffer) {
        final int size = buffer.getInt();
        final Map<String, Object> map = new HashMap<String, Object>(size);
        String key;
        Object value;
        RegisteredClass registeredClass;
        Class<?> valueClass;
        int valueSize;
        for (int i = 0; i < size; i++) {
            key = kryo.readObjectData(buffer, String.class);
            registeredClass = kryo.readClass(buffer);
            // readClass returns null for the class of a null object
            if (registeredClass != null) {
                valueClass = registeredClass.getType();
                if (ArrayList.class.isAssignableFrom(valueClass)) {
                    valueSize = buffer.getInt();
                    final ArrayList<Object> items = new ArrayList<Object>(valueSize);
                    for (int j = 0; j < valueSize; j++) {
                        items.add(kryo.readClassAndObject(buffer));
                    }
                    value = items;
                } else if (String.class.isAssignableFrom(valueClass)) {
                    value = kryo.readObjectData(buffer, String.class);
                } else {
                    throw new IllegalStateException("Unexpected attribute value type " + valueClass);
                }
            } else {
                value = null;
            }
            map.put(key, value);
        }
        return map;
    }
}
