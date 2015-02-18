/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializer for classes that extend {@link org.jasig.cas.authentication.principal.AbstractWebApplicationService}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class AbstractWebApplicationServiceSerializer<T extends AbstractWebApplicationService>
        extends Serializer<T> {
    /** FieldHelper instance. **/
    protected final FieldHelper fieldHelper;

    /**
     * Instantiates a new abstract web application service serializer.
     *
     * @param helper the helper
     */
    public AbstractWebApplicationServiceSerializer(final FieldHelper helper) {
        this.fieldHelper = helper;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final T service) {
        kryo.writeObject(output, service.getId());
        kryo.writeObject(output, fieldHelper.getFieldValue(service, "originalUrl"));
        kryo.writeObject(output, service.getArtifactId());
    }

    @Override
    public T read(final Kryo kryo, final Input input, final Class<T> type) {
        return createService(kryo, input,
                kryo.readObject(input, String.class),
                kryo.readObject(input, String.class),
                kryo.readObject(input, String.class));
    }

    /**
     * Creates the service.
     *
     * @param kryo the Kryo instance
     * @param input the input stream representing the serialized object
     * @param id the id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @return the created service instance.
     */
    protected abstract T createService(final Kryo kryo, final Input input, final String id,
            final String originalUrl, final String artifactId);
}
