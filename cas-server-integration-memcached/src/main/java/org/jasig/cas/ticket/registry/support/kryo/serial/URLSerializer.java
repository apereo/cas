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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;

/**
 * Kryo serializer for {@link URL}.
 * 
 * @author Jerome Leleu
 * @since 4.0.0
 */
public final class URLSerializer extends SimpleSerializer<URL> {

    private final Kryo kryo;
    
    /**
     * @param kryo
     */
    public URLSerializer(final Kryo kryo) {
        this.kryo = kryo;
    }
    
    @Override
    public URL read(final ByteBuffer buffer) {
        final String url = kryo.readObjectData(buffer, String.class);
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(final ByteBuffer buffer, final URL url) {
        kryo.writeObjectData(buffer, url.toExternalForm());
    }
}
