package org.apereo.cas.interrupt;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

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
    public void trackInterrupt(final RequestContext requestContext, final InterruptResponse response) throws Throwable {
        val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val httpResponse = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT, Boolean.TRUE);
        val cookieValue = EncodingUtils.encodeBase64(MAPPER.writeValueAsString(response));
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
        return FunctionUtils.doAndHandle(__ -> {
            val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val cookieValue = casCookieBuilder.retrieveCookieValue(httpRequest);
            LOGGER.debug("Retrieved interrupt cookie value [{}]", cookieValue);
            return StringUtils.isNotBlank(cookieValue)
                ? Optional.ofNullable(MAPPER.readValue(EncodingUtils.decodeBase64ToString(cookieValue), InterruptResponse.class))
                : Optional.<InterruptResponse>empty();
        }, e -> Optional.<InterruptResponse>empty()).apply(requestContext);
    }

    @Override
    public boolean isInterrupted(final RequestContext requestContext) {
        return FunctionUtils.doAndHandle(
                __ -> {
                    val interruptResponse = forCurrentRequest(requestContext);
                    val authentication = WebUtils.getAuthentication(requestContext);
                    return interruptResponse.stream().anyMatch(InterruptResponse::isInterrupt)
                        || (authentication != null && authentication.containsAttribute(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT));
                }, e -> false)
            .apply(requestContext);
    }
}
