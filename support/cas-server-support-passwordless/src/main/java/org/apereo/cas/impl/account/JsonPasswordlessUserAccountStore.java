package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * This is {@link GroovyPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class JsonPasswordlessUserAccountStore extends SimplePasswordlessUserAccountStore {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).singleValueAsArray(true).build().toObjectMapper();

    public JsonPasswordlessUserAccountStore(final Resource resource) {
        super(readFromResource(resource));
    }

    @SneakyThrows
    private static Map<String, PasswordlessUserAccount> readFromResource(final Resource resource) {
        return MAPPER.readValue(resource.getInputStream(), new TypeReference<>() {
        });
    }
}
