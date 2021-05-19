package org.apereo.cas.web.flow;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SingleSignOnParticipationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
public class SingleSignOnParticipationRequest {
    private final RequestContext requestContext;

    private final HttpServletRequest httpServletRequest;

    @Builder.Default
    @Getter
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /**
     * Gets request context.
     *
     * @return the request context
     */
    public Optional<RequestContext> getRequestContext() {
        return Optional.ofNullable(requestContext);
    }

    /**
     * Gets http servlet request.
     *
     * @return the http servlet request
     */
    public Optional<RequestContext> getHttpServletRequest() {
        return Optional.ofNullable(requestContext);
    }

    /**
     * Gets attribute value.
     *
     * @param <T>   the type parameter
     * @param key   the key
     * @param clazz the clazz
     * @return the attribute value
     */
    public <T> T getAttributeValue(final String key, final Class<T> clazz) {
        return clazz.cast(attributes.get(key));
    }

    /**
     * Contains attribute.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean containsAttribute(final String key) {
        return attributes.containsKey(key);
    }
}
