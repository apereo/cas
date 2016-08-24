package org.jasig.cas.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Generic class to serialize objects to/from JSON based on jackson.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractJacksonBackedJsonSerializer<T> implements JsonSerializer<T> {
    private static final long serialVersionUID = -8415599777321259365L;

    private final PrettyPrinter prettyPrinter;

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
            final String jsonString = JsonValue.readHjson(json).toString();
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T fromJson(final File json) {
        try {
            final String jsonString = JsonValue.readHjson(FileUtils.readFileToString(json)).toString();
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T fromJson(final Reader json) {
        try {
            final String jsonString = JsonValue.readHjson(json).toString();
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T fromJson(final InputStream json) {
        try {
            final String jsonString = JsonValue.readHjson(IOUtils.toString(json)).toString();
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void toJson(final OutputStream out, final T object) {
        try (StringWriter writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);
            final String hjsonString = JsonValue.readHjson(writer.toString()).toString(Stringify.HJSON);
            IOUtils.write(hjsonString, out);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void toJson(final Writer out, final T object) {
        try (StringWriter writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);
            JsonValue.readHjson(writer.toString()).writeTo(out, Stringify.FORMATTED);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void toJson(final File out, final T object) {
        try (StringWriter writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);
            JsonValue.readHjson(writer.toString()).writeTo(new BufferedWriter(new FileWriter(out)));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
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
