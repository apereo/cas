package org.apereo.cas.util.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.TypeLiteral;
import lombok.Getter;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This is {@link ObjectMapperHandler}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
public class ObjectMapperHandler<P> {
    private final BiFunction<File, Class<? super P>, Object> reader;
    private final BiConsumer<File, P> writer;
    private final Function<P, String> stringWriter;
    private final ObjectMapper objectMapper;
    private final TypeLiteral<P> typeToken;

    public ObjectMapperHandler(final ObjectMapper mapper) {
        this.objectMapper = mapper;
        this.typeToken = new TypeLiteral<>() {};
        reader = Unchecked.biFunction(objectMapper::readValue);
        writer = Unchecked.biConsumer(objectMapper::writeValue);
        stringWriter = Unchecked.function(objectMapper::writeValueAsString);
    }

    /**
     * Read object from file.
     * @param file file param
     * @return object read from file
     */
    public P readValue(final File file) {
        return (P) reader.apply(file, typeToken.getRawType());
    }

    /**
     * Write to file.
     * @param file file param
     * @param object object to write
     */
    public void writeValue(final File file, final P object) {
        writer.accept(file, object);
    }

    /**
     * Write object to String.
     * @param object object to write
     * @return String representation of +object+
     */
    public String writeValueAsString(final P object) {
        return stringWriter.apply(object);
    }
}
