package org.apereo.cas.support.rest.resources;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
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

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    /**
     * Create response entity for authn failure response.
     *
     * @param e                  the e
     * @param request            the http request
     * @param applicationContext the application context
     * @return the response entity
     */
    public static ResponseEntity<String> createResponseEntityForAuthnFailure(final AuthenticationException e,
                                                                             final HttpServletRequest request,
                                                                             final ApplicationContext applicationContext) {
        try {
            val authnExceptions = e.getHandlerErrors().values()
                .stream()
                .map(ex -> mapExceptionToMessage(e, request, applicationContext, ex))
                .collect(Collectors.toList());
            if (authnExceptions.isEmpty()) {
                authnExceptions.add(mapExceptionToMessage(e, request, applicationContext, e));
            }
            val errorsMap = new HashMap<String, List<String>>(1);
            errorsMap.put("authentication_exceptions", authnExceptions);
            LOGGER.warn("[{}] Caused by: [{}]", e.getMessage(), authnExceptions);

            return new ResponseEntity<>(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(errorsMap), HttpStatus.UNAUTHORIZED);
        } catch (final JsonProcessingException exception) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static String mapExceptionToMessage(final AuthenticationException authnhandlerErrors,
                                                final HttpServletRequest request,
                                                final ApplicationContext applicationContext,
                                                final Throwable ex) {
        val authnMsg = StringUtils.defaultIfBlank(ex.getMessage(), "Authentication Failure: " + authnhandlerErrors.getMessage());
        val authnBundleMsg = getTranslatedMessageForExceptionClass(ex.getClass().getSimpleName(), request, applicationContext);
        return String.format("%s:%s", authnMsg, authnBundleMsg);
    }

    private String getTranslatedMessageForExceptionClass(final String className,
                                                         final HttpServletRequest request,
                                                         final ApplicationContext applicationContext) {
        try {
            val msgKey = MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE + className;
            return applicationContext.getMessage(msgKey, null, request.getLocale());
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }
}
