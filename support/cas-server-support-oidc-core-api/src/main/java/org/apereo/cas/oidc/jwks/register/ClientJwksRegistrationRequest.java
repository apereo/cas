package org.apereo.cas.oidc.jwks.register;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link ClientJwksRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record ClientJwksRegistrationRequest(String proof) {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Convert this record into JSON.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }
}
