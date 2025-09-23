package org.apereo.cas.util.serialization;

import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonValue;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serial;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Generic class to serialize objects to/from JSON based on jackson.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.1
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseJacksonSerializer<T> implements StringSerializer<T> {
    protected static final PrettyPrinter MINIMAL_PRETTY_PRINTER = new MinimalPrettyPrinter();

    @Serial
    private static final long serialVersionUID = -8415599777321259365L;

    @Getter
    private final PrettyPrinter prettyPrinter;
    @Getter
    private final ConfigurableApplicationContext applicationContext;
    @Getter
    private final Class<T> typeToSerialize;
    
    private ObjectReader typeReader;
    private ObjectWriter typeWriter;

    private ObjectMapper objectMapper;
    
    protected BaseJacksonSerializer(
        final ConfigurableApplicationContext applicationContext,
        final Class typeToSerialize) {
        this(new DefaultPrettyPrinter(), applicationContext, typeToSerialize);
    }

    @Override
    public T from(final String json) {
        val jsonString = isJsonFormat() && isLenient() ? readHumanJson(json) : json;
        return readObjectFromString(jsonString);
    }

    @Override
    public T from(final Reader json) {
        return FunctionUtils.doAndHandle(() -> {
            val data = isJsonFormat() && isLenient()
                ? readHumanJson(json)
                : String.join("\n", IOUtils.readLines(json));
            return readObjectFromString(data);
        }, throwable -> null).get();
    }

    @Override
    public T from(final InputStream json) {
        return FunctionUtils.doAndHandle(() -> {
            val jsonString = readJsonFrom(json);
            return readObjectFromString(jsonString);
        }, throwable -> null).get();
    }

    @Override
    public T from(final File json) {
        return FunctionUtils.doAndHandle(() -> {
            val data = isJsonFormat() && isLenient()
                ? readHumanJson(FileUtils.readFileToString(json, StandardCharsets.UTF_8))
                : FileUtils.readFileToString(json, StandardCharsets.UTF_8);
            return readObjectFromString(data);
        }, throwable -> null).get();
    }

    @Override
    public T from(final Writer writer) {
        return from(writer.toString());
    }

    @Override
    public void to(final OutputStream out, final T object) {
        FunctionUtils.doUnchecked(__ -> getTypeWriter().writeValue(out, object));
    }

    @Override
    public void to(final Writer writer, final T object) {
        FunctionUtils.doUnchecked(__ -> getTypeWriter().writeValue(writer, object));
    }

    @Override
    public void to(final File out, final T object) {
        FunctionUtils.doUnchecked(__ -> getTypeWriter().writeValue(out, object));
    }

    @Override
    public String toString(final T object) {
        return FunctionUtils.doUnchecked(() -> {
            try (val writer = new StringWriter()) {
                to(writer, object);
                return writer.toString();
            }
        });
    }

    @Override
    public String fromList(final Collection<T> object) {
        return FunctionUtils.doUnchecked(() -> {
            try (val writer = new StringWriter()) {
                getObjectMapper().writerFor(object.getClass()).with(prettyPrinter).writeValue(writer, object);
                return writer.toString();
            }
        });
    }

    @Override
    public List<T> fromList(final String json) {
        val jsonString = isJsonFormat() && isLenient() ? readHumanJson(json) : json;
        return readObjectsFromString(jsonString);
    }

    @Override
    public T merge(final T baseEntity, final T childEntity) {
        return FunctionUtils.doUnchecked(() -> {
            val reader = getObjectMapper().readerForUpdating(baseEntity);
            return reader.readValue(toString(childEntity));
        });
    }

    protected boolean isLenient() {
        return false;
    }

    /**
     * Gets object mapper and builds on if uninitialized.
     *
     * @return the object mapper
     */
    public ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = JacksonObjectMapperFactory
                .builder()
                .defaultTypingEnabled(isDefaultTypingEnabled())
                .jsonFactory(getJsonFactory())
                .applicationContext(applicationContext)
                .build()
                .toObjectMapper();
            configureObjectMapper(objectMapper);
        }
        return objectMapper;
    }

    protected void configureObjectMapper(final ObjectMapper objectMapper) {
    }

    protected String readJsonFrom(final InputStream json) throws IOException {
        return isJsonFormat() && isLenient()
            ? readHumanJson(IOUtils.toString(json, StandardCharsets.UTF_8))
            : String.join("\n", IOUtils.readLines(json, StandardCharsets.UTF_8));
    }
    

    protected boolean isDefaultTypingEnabled() {
        return true;
    }

    protected JsonFactory getJsonFactory() {
        return null;
    }

    protected T readObjectFromString(final String jsonString) {
        try {
            LOGGER.trace("Attempting to parse [{}]", jsonString);
            return getTypeReader().readValue(jsonString);
        } catch (final Exception e) {
            LOGGER.error("Cannot read/parse [{}] to deserialize into type [{}]. This may be caused "
                    + "in the absence of a configuration/support module that knows how to interpret the fragment, "
                    + "specially if the fragment describes a CAS registered service definition. "
                    + "Internal parsing error is [{}]",
                DigestUtils.abbreviate(jsonString), getTypeToSerialize(), e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    protected List<T> readObjectsFromString(final String jsonString) {
        try {
            LOGGER.trace("Attempting to consume [{}]", jsonString);
            val expectedType = getObjectMapper().getTypeFactory().constructParametricType(List.class, getTypeToSerialize());
            return getObjectMapper().readerFor(expectedType).readValue(jsonString);
        } catch (final Exception e) {
            LOGGER.error("Cannot read/parse [{}] to deserialize into List of type [{}]."
                    + "Internal parsing error is [{}]",
                DigestUtils.abbreviate(jsonString), getTypeToSerialize(), e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    private boolean isJsonFormat() {
        return !(getObjectMapper().getFactory() instanceof YAMLFactory);
    }

    private static String readHumanJson(final String json) {
        return JsonValue.readHjson(json).toString();
    }

    private static String readHumanJson(final Reader json) throws Exception {
        return JsonValue.readHjson(json).toString();
    }

    protected ObjectReader getTypeReader() {
        if (typeReader == null) {
            typeReader = getObjectMapper().readerFor(getTypeToSerialize());
        }
        return typeReader;
    }

    protected ObjectWriter getTypeWriter() {
        if (typeWriter == null) {
            typeWriter = getObjectMapper().writerFor(typeToSerialize).with(prettyPrinter);
        }
        return typeWriter;
    }

}
