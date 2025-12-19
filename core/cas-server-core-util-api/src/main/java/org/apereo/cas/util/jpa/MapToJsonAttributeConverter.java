package org.apereo.cas.util.jpa;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * This is {@link MapToJsonAttributeConverter}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Converter
public class MapToJsonAttributeConverter implements AttributeConverter<Map<String, ?>, String> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .minimal(true).defaultTypingEnabled(true).build().toObjectMapper();

    @Override
    public String convertToDatabaseColumn(final Map<String, ?> map) {
        return MAPPER.writeValueAsString(map);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(final String value) {
        return MAPPER.readValue(value, new TypeReference<>() {
        });
    }
}
