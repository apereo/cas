package org.apereo.cas.util.jpa;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;

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
    public String convertToDatabaseColumn(final Map<String, ? extends Object> map) {
        return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(map));
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(final String value) {
        return FunctionUtils.doUnchecked(() -> MAPPER.readValue(value, new TypeReference<>() {
        }));
    }
}
