package org.apereo.cas.rest.factory;

import module java.base;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultUserAuthenticationResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultUserAuthenticationResourceEntityResponseFactory implements UserAuthenticationResourceEntityResponseFactory {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Override
    public ResponseEntity<String> build(final AuthenticationResult result,
                                        final HttpServletRequest request) throws Exception {
        return new ResponseEntity<>(MAPPER.writeValueAsString(result), HttpStatus.OK);
    }
}
