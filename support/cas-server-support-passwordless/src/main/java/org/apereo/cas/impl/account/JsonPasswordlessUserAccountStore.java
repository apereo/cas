package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    public JsonPasswordlessUserAccountStore(final Resource resource) {
        super(readFromResource(resource));
    }

    @SneakyThrows
    private static Map<String, PasswordlessUserAccount> readFromResource(final Resource resource) {
        return MAPPER.readValue(resource.getInputStream(), new TypeReference<>() {
        });
    }
}
