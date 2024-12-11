package org.apereo.cas.util.jpa;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import java.util.Map;

/**
 * This is {@link MultivaluedMapToJsonAttributeConverter}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Converter
public class MultivaluedMapToJsonAttributeConverter implements AttributeConverter<Map<String, List<Object>>, String> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Override
    public String convertToDatabaseColumn(final Map<String, List<Object>> map) {
        return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(map));
    }

    @Override
    public Map<String, List<Object>> convertToEntityAttribute(final String value) {
        return FunctionUtils.doUnchecked(() -> MAPPER.readValue(value, new TypeReference<>() {
        }));
    }
}
