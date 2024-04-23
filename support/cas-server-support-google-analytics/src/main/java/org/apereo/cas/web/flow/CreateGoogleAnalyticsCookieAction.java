package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * This is {@link CreateGoogleAnalyticsCookieAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CreateGoogleAnalyticsCookieAction extends BaseCasWebflowAction {
    private final CasConfigurationProperties casProperties;

    private final CasCookieBuilder googleAnalyticsCookieBuilder;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authn = WebUtils.getAuthentication(requestContext);
        val attributes = new LinkedHashMap<>(authn.getAttributes());
        attributes.putAll(authn.getPrincipal().getAttributes());

        val cookie = casProperties.getGoogleAnalytics().getCookie();
        val attributeName = cookie.getAttributeName();
        val attributeValuePattern = RegexUtils.createPattern(cookie.getAttributeValuePattern());

        LOGGER.trace("Available attributes are [{}] examined against cookie attribute name [{}] with value pattern [{}]",
            attributeName, attributeName, attributeValuePattern.pattern());

        if (StringUtils.isNotBlank(attributeName) && attributes.containsKey(attributeName)) {
            val values = CollectionUtils.toCollection(attributes.get(attributeName));
            LOGGER.trace("Attribute values found for [{}] are [{}]", attributeName, values);
            val cookieValue = values
                .stream()
                .map(Object::toString)
                .filter(value -> RegexUtils.find(attributeValuePattern, value))
                .collect(Collectors.joining(","));
            LOGGER.trace("Google analytics final cookie value is [{}]", cookieValue);

            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            googleAnalyticsCookieBuilder.addCookie(request, response,
                CookieRetrievingCookieGenerator.isRememberMeAuthentication(requestContext), cookieValue);
        }
        return null;
    }
}
