package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    private final HttpServletResponse httpServletResponse;

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

    public Optional<HttpServletRequest> getHttpServletRequest() {
        return Optional.ofNullable(httpServletRequest);
    }

    public Optional<HttpServletResponse> getHttpServletResponse() {
        return Optional.ofNullable(httpServletResponse);
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
     * @return true/false
     */
    public boolean containsAttribute(final String key) {
        return attributes.containsKey(key);
    }

    /**
     * Put attribute.
     *
     * @param key   the key
     * @param value the value
     * @return the single sign on participation request
     */
    @CanIgnoreReturnValue
    public SingleSignOnParticipationRequest attribute(final String key, final Object value) {
        this.attributes.put(key, value);
        return this;
    }

    /**
     * Is requesting renew authentication.
     *
     * @return true or false.
     */
    public boolean isRequestingRenewAuthentication() {
        return getRequestParameter(CasProtocolConstants.PARAMETER_RENEW).isPresent();
    }

    /**
     * Gets request parameter.
     *
     * @param key the key
     * @return the request parameter
     */
    public Optional<String> getRequestParameter(final String key) {
        val result = getHttpServletRequest()
            .map(request -> request.getParameter(key))
            .filter(StringUtils::isNotBlank)
            .orElseGet(() -> getRequestContext().map(context -> context.getRequestParameters().get(key)).orElse(null));
        return Optional.ofNullable(result);
    }
}
