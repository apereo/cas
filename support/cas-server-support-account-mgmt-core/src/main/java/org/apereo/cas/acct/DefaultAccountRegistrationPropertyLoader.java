package org.apereo.cas.acct;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hjson.JsonValue;
import org.jooq.lambda.Unchecked;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is {@link DefaultAccountRegistrationPropertyLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class DefaultAccountRegistrationPropertyLoader implements AccountRegistrationPropertyLoader {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true)
        .failOnUnknownProperties(true)
        .singleValueAsArray(true)
        .build()
        .toObjectMapper();

    private final Resource resource;

    @Override
    public Map<String, AccountRegistrationProperty> load() {
        return Unchecked.supplier(() -> {
            try (val reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                val personList = new TypeReference<Map<String, AccountRegistrationProperty>>() {
                };
                return MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            }
        }).get();
    }

    @Override
    public AccountRegistrationPropertyLoader store(final Map<String, AccountRegistrationProperty> map) {
        Unchecked.consumer(input -> MAPPER.writerWithDefaultPrettyPrinter().writeValue(resource.getFile(), input)).accept(map);
        return this;
    }
}
