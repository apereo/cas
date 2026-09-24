package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link SimpleInterruptTrackingEngine}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class SimpleInterruptTrackingEngine implements InterruptTrackingEngine {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final CasCookieBuilder casCookieBuilder;

    private final CasConfigurationProperties casProperties;

    @Override
    public void trackInterrupt(final RequestContext requestContext, final InterruptResponse response) {
        val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val httpResponse = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT, Boolean.TRUE);
        val tracked = new TrackedInterrupt(authentication.getPrincipal().getId(), response);
        val cookieValue = EncodingUtils.encodeBase64(MAPPER.writeValueAsString(tracked));
        LOGGER.debug("Storing interrupt response as base64 cookie [{}]", cookieValue);

        if (casProperties.getInterrupt().getCookie().isAutoConfigureCookiePath()) {
            CookieUtils.configureCookiePath(httpRequest, casCookieBuilder);
        }
        val cookie = casCookieBuilder.addCookie(httpRequest, httpResponse, cookieValue);
        LOGGER.debug("Added interrupt cookie [{}] with value [{}]", cookie.getName(), cookie.getValue());
    }

    @Override
    public void removeInterrupt(final RequestContext requestContext) {
        val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        if (casProperties.getInterrupt().getCookie().isAutoConfigureCookiePath()) {
            CookieUtils.configureCookiePath(httpRequest, casCookieBuilder);
        }
        val httpResponse = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        casCookieBuilder.removeCookie(httpResponse);
    }

    @Override
    public Optional<InterruptResponse> forCurrentRequest(final RequestContext requestContext) {
        return FunctionUtils.doAndHandle(_ -> {
            val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val cookieValue = casCookieBuilder.retrieveCookieValue(httpRequest);
            LOGGER.debug("Retrieved interrupt cookie value [{}]", cookieValue);
            val authentication = WebUtils.getAuthentication(requestContext);
            if (StringUtils.isBlank(cookieValue) || authentication == null) {
                return Optional.<InterruptResponse>empty();
            }
            val tracked = MAPPER.readValue(EncodingUtils.decodeBase64ToString(cookieValue), TrackedInterrupt.class);
            return Optional.ofNullable(tracked.response())
                .filter(_ -> authentication.getPrincipal().getId().equals(tracked.principal()));
        }, e -> Optional.<InterruptResponse>empty()).apply(requestContext);
    }

    @Override
    public boolean isInterrupted(final RequestContext requestContext) {
        return FunctionUtils.doAndHandle(
                _ -> {
                    val interruptResponse = forCurrentRequest(requestContext);
                    val authentication = WebUtils.getAuthentication(requestContext);
                    return interruptResponse.stream().anyMatch(InterruptResponse::isInterrupt)
                        || (authentication != null && authentication.containsAttribute(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT));
                }, e -> false)
            .apply(requestContext);
    }

    /**
     * Interrupt response tracked for the principal that finalized it.
     *
     * @param principal the principal id
     * @param response  the interrupt response
     */
    public record TrackedInterrupt(@Nullable String principal, @Nullable InterruptResponse response) {
    }
}
