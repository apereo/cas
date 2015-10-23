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
package org.jasig.cas.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Generic class to serialize objects to/from JSON based on jackson.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractJacksonBackedJsonSerializer<T> implements JsonSerializer<T> {
    private static final long serialVersionUID = -8415599777321259365L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJacksonBackedJsonSerializer.class);

    /**
     * The Pretty printer.
     */
    private final PrettyPrinter prettyPrinter;

    /**
     * The Object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Registered service json serializer.
     * Uses the {@link com.fasterxml.jackson.core.util.DefaultPrettyPrinter} for formatting.
     */
    public AbstractJacksonBackedJsonSerializer() {
        this(new DefaultPrettyPrinter());
    }

    /**
     * Instantiates a new Registered service json serializer.
     *
     * @param prettyPrinter the pretty printer
     */
    public AbstractJacksonBackedJsonSerializer(final PrettyPrinter prettyPrinter) {
        this.objectMapper = initializeObjectMapper();
        this.prettyPrinter = prettyPrinter;
    }

    /**
     * Instantiates a new Registered service json serializer.
     *
     * @param objectMapper  the object mapper
     * @param prettyPrinter the pretty printer
     */
    public AbstractJacksonBackedJsonSerializer(final ObjectMapper objectMapper, final PrettyPrinter prettyPrinter) {
        this.objectMapper = objectMapper;
        this.prettyPrinter = prettyPrinter;
    }

    @Override
    public T fromJson(final String json) {
        try {
            return this.objectMapper.readValue(json, getTypeToSerialize());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T fromJson(final File json) {
        try {
            return this.objectMapper.readValue(json, getTypeToSerialize());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T fromJson(final Reader json) {
        try {
            return this.objectMapper.readValue(json, getTypeToSerialize());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T fromJson(final InputStream json) {
        try {
            return this.objectMapper.readValue(json, getTypeToSerialize());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toJson(final OutputStream out, final T object) {
        try {
            this.objectMapper.writer(this.prettyPrinter).writeValue(out, object);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toJson(final Writer out, final T object) {
        try {
            this.objectMapper.writer(this.prettyPrinter).writeValue(out, object);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toJson(final File out, final T object) {
        try {
            this.objectMapper.writer(this.prettyPrinter).writeValue(out, object);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize object mapper.
     *
     * @return the object mapper
     */
    protected ObjectMapper initializeObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

    /**
     * Gets type to serialize.
     *
     * @return the type to serialize
     */
    protected abstract Class<T> getTypeToSerialize();
}
