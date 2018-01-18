package org.apereo.cas.support.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link RestResourceUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@UtilityClass
public class RestResourceUtils {

    private static ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    /**
     * Create response entity for authn failure response.
     *
     * @param e the e
     * @return the response entity
     */
    public static ResponseEntity<String> createResponseEntityForAuthnFailure(final AuthenticationException e) {
        try {
            final List<String> authnExceptions = e.getHandlerErrors().values()
                .stream()
                .map(ex -> ex.getClass().getSimpleName()
                    + ": "
                    + StringUtils.defaultIfBlank(ex.getMessage(), "Authentication Failure: " + e.getMessage()))
                .collect(Collectors.toList());
            final Map<String, List<String>> errorsMap = new HashMap<>();
            errorsMap.put("authentication_exceptions", authnExceptions);
            LOGGER.warn("[{}] Caused by: [{}]", e.getMessage(), authnExceptions);
            return new ResponseEntity<>(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(errorsMap), HttpStatus.UNAUTHORIZED);
        } catch (final JsonProcessingException exception) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
