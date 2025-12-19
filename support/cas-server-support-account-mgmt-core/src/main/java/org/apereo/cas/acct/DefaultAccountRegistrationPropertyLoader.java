package org.apereo.cas.acct;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hjson.JsonValue;
import org.jooq.lambda.Unchecked;
import org.springframework.core.io.Resource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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
    @CanIgnoreReturnValue
    public AccountRegistrationPropertyLoader store(final Map<String, AccountRegistrationProperty> map) {
        Unchecked.consumer(input -> MAPPER.writerWithDefaultPrettyPrinter().writeValue(resource.getFile(), input)).accept(map);
        return this;
    }
}
