package org.apereo.cas.util.serialization;

import org.apereo.cas.util.DigestUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Generic class to serialize objects to/from JSON based on jackson.
 *
 * @param <T> the type parameter
 * @author Misagh Moayyed
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

    private static final long serialVersionUID = -8415599777321259365L;

    private final ObjectMapper objectMapper;
    
    private final transient PrettyPrinter prettyPrinter;

    /**
     * Instantiates a new Registered service json serializer.
     * Uses the {@link com.fasterxml.jackson.core.util.DefaultPrettyPrinter} for formatting.
     */
    protected AbstractJacksonBackedStringSerializer() {
        this(new DefaultPrettyPrinter());
    }

    /**
     * Instantiates a new Registered service json serializer.
     *
     * @param prettyPrinter the pretty printer
     */
    protected AbstractJacksonBackedStringSerializer(final PrettyPrinter prettyPrinter) {
        this.objectMapper = initializeObjectMapper();
        this.prettyPrinter = prettyPrinter;
    }

    private boolean isJsonFormat() {
        return !(this.objectMapper.getFactory() instanceof YAMLFactory);
    }

    @Override
    @SneakyThrows
    public T from(final String json) {
        val jsonString = isJsonFormat() ? JsonValue.readHjson(json).toString() : json;
        return readObjectFromString(jsonString);
    }

    @Override
    @SneakyThrows
    public T from(final File json) {
        val string = isJsonFormat()
            ? JsonValue.readHjson(FileUtils.readFileToString(json, StandardCharsets.UTF_8)).toString()
            : FileUtils.readFileToString(json, StandardCharsets.UTF_8);
        return readObjectFromString(string);
    }

    @Override
    @SneakyThrows
    public T from(final Reader json) {
        val string = isJsonFormat()
            ? JsonValue.readHjson(json).toString()
            : String.join("\n", IOUtils.readLines(json));
        return readObjectFromString(string);
    }

    @Override
    public T from(final Writer writer) {
        return from(writer.toString());
    }

    @Override
    @SneakyThrows
    public T from(final InputStream json) {
        val jsonString = readJsonFrom(json);
        return readObjectFromString(jsonString);
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
    @SneakyThrows
    public void to(final OutputStream out, final T object) {
        try (val writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);
            val hjsonString = isJsonFormat()
                ? JsonValue.readHjson(writer.toString()).toString(Stringify.HJSON)
                : writer.toString();
            IOUtils.write(hjsonString, out, StandardCharsets.UTF_8);
        }
    }

    @Override
    @SneakyThrows
    public void to(final Writer out, final T object) {
        try (val writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);

            if (isJsonFormat()) {
                val opt = this.prettyPrinter instanceof MinimalPrettyPrinter ? Stringify.PLAIN : Stringify.FORMATTED;
                JsonValue.readHjson(writer.toString()).writeTo(out, opt);
            } else {
                IOUtils.write(writer.toString(), out);
            }
        }
    }

    @Override
    @SneakyThrows
    public void to(final File out, final T object) {
        try (val writer = new StringWriter()) {
            this.objectMapper.writer(this.prettyPrinter).writeValue(writer, object);

            if (isJsonFormat()) {
                try (val fileWriter = Files.newBufferedWriter(out.toPath(), StandardCharsets.UTF_8)) {
                    val opt = this.prettyPrinter instanceof MinimalPrettyPrinter ? Stringify.PLAIN : Stringify.FORMATTED;
                    JsonValue.readHjson(writer.toString()).writeTo(fileWriter, opt);
                    fileWriter.flush();
                }
            } else {
                FileUtils.write(out, writer.toString(), StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    @SneakyThrows
    public String toString(final T object) {
        try (val writer = new StringWriter()) {
            to(writer, object);
            return writer.toString();
        }
    }

    /**
     * Initialize object mapper.
     *
     * @return the object mapper
     */
    protected ObjectMapper initializeObjectMapper() {
        val mapper = JacksonObjectMapperFactory
            .builder()
            .defaultTypingEnabled(isDefaultTypingEnabled())
            .jsonFactory(getJsonFactory())
            .build()
            .toObjectMapper();
        configureObjectMapper(mapper);
        return mapper;
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
            return this.objectMapper.readValue(jsonString, getTypeToSerialize());
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
}
