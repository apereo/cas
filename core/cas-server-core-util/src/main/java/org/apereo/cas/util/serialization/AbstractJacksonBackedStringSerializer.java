package org.apereo.cas.util.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.util.DigestUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJacksonBackedStringSerializer.class);

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
            return readObjectFromJson(jsonString);
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

            return readObjectFromJson(jsonString);
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

            return readObjectFromJson(jsonString);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T from(final Writer writer) {
        return from(writer.toString());
    }

    @Override
    public T from(final InputStream json) {
        try {
            final String jsonString = readJsonFrom(json);
            return readObjectFromJson(jsonString);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Read json from stream.
     *
     * @param json the json
     * @return the string
     * @throws IOException the io exception
     */
    protected String readJsonFrom(final InputStream json) throws IOException {
        return isJsonFormat()
                ? JsonValue.readHjson(IOUtils.toString(json, StandardCharsets.UTF_8)).toString()
                : IOUtils.readLines(json, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
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
                final Stringify opt = this.prettyPrinter instanceof MinimalPrettyPrinter ? Stringify.PLAIN : Stringify.FORMATTED;
                JsonValue.readHjson(writer.toString()).writeTo(out, opt);
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
                try (FileWriter fileWriter = new FileWriter(out);
                     BufferedWriter buffer = new BufferedWriter(fileWriter)) {

                    final Stringify opt = this.prettyPrinter instanceof MinimalPrettyPrinter ? Stringify.PLAIN : Stringify.FORMATTED;
                    JsonValue.readHjson(writer.toString()).writeTo(buffer, opt);
                    buffer.flush();
                    fileWriter.flush();
                }
            } else {
                FileUtils.write(out, writer.toString(), StandardCharsets.UTF_8);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Initialize object mapper.
     *
     * @return the object mapper
     */
    protected ObjectMapper initializeObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper(getJsonFactory());
        configureObjectMapper(mapper);
        return mapper;
    }

    /**
     * Configure mapper.
     *
     * @param mapper the mapper
     */
    protected void configureObjectMapper(final ObjectMapper mapper) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);

        if (isDefaultTypingEnabled()) {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }
        mapper.findAndRegisterModules();
    }

    protected boolean isDefaultTypingEnabled() {
        return true;
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

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Read object from json.
     *
     * @param jsonString the json string
     * @return the type
     */
    protected T readObjectFromJson(final String jsonString) {
        try {
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
        } catch (final Exception e) {
            LOGGER.error("Cannot read/parse JSON [{}] to deserialize into type [{}]. This may be caused "
                            + "in the absence of a configuration/support module that knows how to interpret the JSON fragment, "
                            + "specially if the fragment describes a CAS registered service definition. "
                            + "Internal parsing error is [{}]",
                    DigestUtils.abbreviate(jsonString), getTypeToSerialize(), e.getMessage());
            LOGGER.trace(e.getMessage(), e);
        }
        return null;
    }
}
