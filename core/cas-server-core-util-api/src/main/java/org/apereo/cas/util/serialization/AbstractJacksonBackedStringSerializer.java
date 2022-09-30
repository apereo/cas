package org.apereo.cas.util.serialization;

import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serial;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Generic class to serialize objects to/from JSON based on jackson.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.1
 */
@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractJacksonBackedStringSerializer<T> implements StringSerializer<T> {
    /**
     * Minimal pretty printer instance.
     */
    protected static final PrettyPrinter MINIMAL_PRETTY_PRINTER = new MinimalPrettyPrinter();

    @Serial
    private static final long serialVersionUID = -8415599777321259365L;

    private final PrettyPrinter prettyPrinter;

    private ObjectMapper objectMapper;

    protected AbstractJacksonBackedStringSerializer() {
        this(new DefaultPrettyPrinter());
    }

    private boolean isJsonFormat() {
        return !(getObjectMapper().getFactory() instanceof YAMLFactory);
    }

    @Override
    public List<T> fromList(final String json) {
        val jsonString = isJsonFormat() ? JsonValue.readHjson(json).toString() : json;
        return readObjectsFromString(jsonString);
    }

    @Override
    public T from(final String json) {
        val jsonString = isJsonFormat() ? JsonValue.readHjson(json).toString() : json;
        return readObjectFromString(jsonString);
    }

    @Override
    public T from(final File json) {
        return FunctionUtils.doAndHandle(() -> {
            val data = isJsonFormat()
                ? JsonValue.readHjson(FileUtils.readFileToString(json, StandardCharsets.UTF_8)).toString()
                : FileUtils.readFileToString(json, StandardCharsets.UTF_8);
            return readObjectFromString(data);
        }, throwable -> null).get();
    }

    @Override
    public T from(final Reader json) {
        return FunctionUtils.doAndHandle(() -> {
            val data = isJsonFormat()
                ? JsonValue.readHjson(json).toString()
                : String.join("\n", IOUtils.readLines(json));
            return readObjectFromString(data);
        }, throwable -> null).get();
    }

    @Override
    public T from(final Writer writer) {
        return from(writer.toString());
    }

    @Override
    public T from(final InputStream json) {
        return FunctionUtils.doAndHandle(() -> {
            val jsonString = readJsonFrom(json);
            return readObjectFromString(jsonString);
        }, throwable -> null).get();
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
            : String.join("\n", IOUtils.readLines(json, StandardCharsets.UTF_8));
    }

    @Override
    public void to(final OutputStream out, final T object) {
        FunctionUtils.doUnchecked(unused -> {
            try (val writer = new StringWriter()) {
                getObjectMapper().writer(this.prettyPrinter).writeValue(writer, object);
                val hjsonString = isJsonFormat()
                    ? JsonValue.readHjson(writer.toString()).toString(getJsonFormattingOptions())
                    : writer.toString();
                IOUtils.write(hjsonString, out, StandardCharsets.UTF_8);
            }
        });
    }

    @Override
    public void to(final Writer out, final T object) {
        FunctionUtils.doUnchecked(unused -> {
            try (val writer = new StringWriter()) {
                getObjectMapper().writer(this.prettyPrinter).writeValue(writer, object);
                if (isJsonFormat()) {
                    JsonValue.readHjson(writer.toString()).writeTo(out, getJsonFormattingOptions());
                } else {
                    IOUtils.write(writer.toString(), out);
                }
            }
        });
    }

    @Override
    public void to(final File out, final T object) {
        FunctionUtils.doUnchecked(unused -> {
            try (val writer = new StringWriter()) {
                getObjectMapper().writer(this.prettyPrinter).writeValue(writer, object);

                if (isJsonFormat()) {
                    try (val fileWriter = Files.newBufferedWriter(out.toPath(), StandardCharsets.UTF_8)) {
                        JsonValue.readHjson(writer.toString()).writeTo(fileWriter, getJsonFormattingOptions());
                        fileWriter.flush();
                    }
                } else {
                    FileUtils.write(out, writer.toString(), StandardCharsets.UTF_8);
                }
            }
        });
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

    /**
     * Configure mapper.
     *
     * @param mapper the mapper
     */
    protected void configureObjectMapper(final ObjectMapper mapper) {
    }

    protected boolean isDefaultTypingEnabled() {
        return true;
    }

    /**
     * Gets json factory.
     *
     * @return the json factory
     */
    protected JsonFactory getJsonFactory() {
        return null;
    }

    /**
     * Read object from json.
     *
     * @param jsonString the json string
     * @return the type
     */
    protected T readObjectFromString(final String jsonString) {
        try {
            LOGGER.trace("Attempting to consume [{}]", jsonString);
            return getObjectMapper().readValue(jsonString, getTypeToSerialize());
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
            return getObjectMapper().readValue(jsonString, expectedType);
        } catch (final Exception e) {
            LOGGER.error("Cannot read/parse [{}] to deserialize into List of type [{}]."
                         + "Internal parsing error is [{}]",
                DigestUtils.abbreviate(jsonString), getTypeToSerialize(), e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets object mapper and builds on if uninitialized.
     *
     * @return the object mapper
     */
    @Synchronized
    public ObjectMapper getObjectMapper() {
        if (this.objectMapper == null) {
            this.objectMapper = JacksonObjectMapperFactory
                .builder()
                .defaultTypingEnabled(isDefaultTypingEnabled())
                .jsonFactory(getJsonFactory())
                .build()
                .toObjectMapper();
            configureObjectMapper(objectMapper);
        }
        return this.objectMapper;
    }

    private Stringify getJsonFormattingOptions() {
        return prettyPrinter instanceof MinimalPrettyPrinter ? Stringify.PLAIN : Stringify.FORMATTED;
    }
}
