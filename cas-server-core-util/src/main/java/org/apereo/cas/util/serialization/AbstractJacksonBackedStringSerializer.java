package org.apereo.cas.util.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Throwables;
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
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Generic class to serialize objects to/from JSON based on jackson.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.1
 */
public abstract class AbstractJacksonBackedStringSerializer<T> implements StringSerializer<T> {
    private static final long serialVersionUID = -8415599777321259365L;
    
    private final PrettyPrinter prettyPrinter;
    
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Registered service json serializer.
     * Uses the {@link com.fasterxml.jackson.core.util.DefaultPrettyPrinter} for formatting.
     */
    public AbstractJacksonBackedStringSerializer() {
        this(new DefaultPrettyPrinter());
    }

    /**
     * Instantiates a new Registered service json serializer.
     *
     * @param prettyPrinter the pretty printer
     */
    public AbstractJacksonBackedStringSerializer(final PrettyPrinter prettyPrinter) {
        this.objectMapper = initializeObjectMapper();
        this.prettyPrinter = prettyPrinter;
    }

    /**
     * Instantiates a new Registered service json serializer.
     *
     * @param objectMapper  the object mapper
     * @param prettyPrinter the pretty printer
     */
    public AbstractJacksonBackedStringSerializer(final ObjectMapper objectMapper, final PrettyPrinter prettyPrinter) {
        this.objectMapper = objectMapper;
        this.prettyPrinter = prettyPrinter;
    }

    private boolean isJsonFormat() {
        return !(this.objectMapper.getFactory() instanceof YAMLFactory);
    }
    
    @Override
    public T from(final String json) {
        try {
            final String jsonString = isJsonFormat() ? JsonValue.readHjson(json).toString() : json;
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T from(final File json) {
        try {
            final String jsonString = isJsonFormat() 
                    ? JsonValue.readHjson(FileUtils.readFileToString(json, StandardCharsets.UTF_8)).toString()
                    : FileUtils.readFileToString(json, StandardCharsets.UTF_8);
            
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T from(final Reader json) {
        try {
            final String jsonString = isJsonFormat() 
                    ? JsonValue.readHjson(json).toString()
                    : IOUtils.readLines(json).stream().collect(Collectors.joining());
            
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T from(final InputStream json) {
        try {
            final String jsonString = isJsonFormat() 
                    ? JsonValue.readHjson(IOUtils.toString(json, StandardCharsets.UTF_8)).toString()
                    : IOUtils.readLines(json, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
            
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void to(final OutputStream out, final T object) {
        try (StringWriter writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);
            final String hjsonString = isJsonFormat() 
                    ? JsonValue.readHjson(writer.toString()).toString(Stringify.HJSON)
                    : writer.toString();
            
            IOUtils.write(hjsonString, out, StandardCharsets.UTF_8);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void to(final Writer out, final T object) {
        try (StringWriter writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);
            
            if (isJsonFormat()) {
                JsonValue.readHjson(writer.toString()).writeTo(out, Stringify.FORMATTED);
            } else {
                IOUtils.write(writer.toString(), out);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void to(final File out, final T object) {
        try (StringWriter writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);
            
            if (isJsonFormat()) {
                JsonValue.readHjson(writer.toString()).writeTo(new BufferedWriter(new FileWriter(out)));
            } else {
                FileUtils.write(out, writer.toString(), StandardCharsets.UTF_8);
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
    
    /**
     * Initialize object mapper.
     *
     * @return the object mapper
     */
    protected ObjectMapper initializeObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper(getJsonFactory());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

    protected JsonFactory getJsonFactory() {
        return null;
    }
    
    /**
     * Gets type to serialize.
     *
     * @return the type to serialize
     */
    protected abstract Class<T> getTypeToSerialize();
}
