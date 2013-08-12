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

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import org.jasig.cas.support.saml.authentication.principal.SamlService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.util.HttpClient;
import org.jasig.cas.util.SimpleHttpClient;

/**
 * Serializer for {@link SamlService} class.
 *
 * @author Marvin S. Addison
 */
public final class SamlServiceSerializer extends AbstractWebApplicationServiceSerializer<SamlService> {
    private static final Constructor CONSTRUCTOR;

    static {
        try {
            CONSTRUCTOR = SamlService.class.getDeclaredConstructor(
                    String.class, String.class, String.class, HttpClient.class, String.class);
            CONSTRUCTOR.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Expected constructor signature not found.", e);
        }
    }

    public SamlServiceSerializer(final Kryo kryo, final FieldHelper helper) {
        super(kryo, helper);
    }

    public void write(final ByteBuffer buffer, final SamlService service) {
        super.write(buffer, service);
        kryo.writeObject(buffer, service.getRequestID());
    }

    protected SamlService createService(
            final ByteBuffer buffer,
            final String id,
            final String originalUrl,
            final String artifactId) {

        final String requestId = kryo.readObject(buffer, String.class);
        try {
            return (SamlService) CONSTRUCTOR.newInstance(id, originalUrl, artifactId, new SimpleHttpClient(), requestId);
        } catch (final Exception e) {
            throw new IllegalStateException("Error creating SamlService", e);
        }
    }
}
