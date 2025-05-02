package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.RedirectView;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20TokenAuthorizationResponseBuilderTests}.
 *
 * @author David Albrecht
 * @since 6.1.3
 */
@Tag("OAuth")
class OAuth20TokenAuthorizationResponseBuilderTests extends AbstractOAuth20Tests {
    private static final String STATE = "{\"d\":\"value\"}";

    private static final String NONCE = "%123=";

    private static void verifyParam(final Map<String, List<String>> params, final String paramName, final String expectedParamValue) {
        assertTrue(params.containsKey(paramName), () -> "Expected " + paramName + "  param in redirect URL");
        assertEquals(1,
            params
                .get(paramName)
                .size(),
            () -> "Expected one value for " + paramName + " param");
        assertEquals(expectedParamValue,
            params
                .get(paramName)
                .getFirst(),
            () -> "Expected unchanged " + paramName + "  param");
    }

    private static Map<String, List<String>> splitQuery(final String fragment) {
        if (StringUtils.isBlank(fragment)) {
            return new HashMap<>();
        }
        return Arrays
            .stream(fragment.split("&"))
            .map(OAuth20TokenAuthorizationResponseBuilderTests::splitQueryParameter)
            .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey,
                LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private static AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(final String it) {
        val idx = it.indexOf('=');
        val key = idx > 0 ? it.substring(0, idx) : it;
        val value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    @Test
    void verifyUnchangedStateAndNonceParameter() throws Throwable {
        assertEquals(Ordered.LOWEST_PRECEDENCE, oauthAuthorizationCodeResponseBuilder.getOrder());

        val registeredService = getRegisteredService("example", CLIENT_SECRET, new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val service = RegisteredServiceTestUtils.getService("example");
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, Collections.singletonList(STATE));
        attributes.put(OAuth20Constants.NONCE, Collections.singletonList(NONCE));

        val holder = AccessTokenRequestContext
            .builder()
            .clientId(registeredService.getClientId())
            .service(service)
            .authentication(RegisteredServiceTestUtils.getAuthentication(RegisteredServiceTestUtils.getPrincipal(ID), attributes))
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.NONE)
            .responseType(OAuth20ResponseTypes.TOKEN)
            .ticketGrantingTicket(new MockTicketGrantingTicket(ID))
            .generateRefreshToken(true)
            .redirectUri("https://oauth.example.org")
            .build();
        val modelAndView = oauthTokenResponseBuilder.build(holder);
        assertInstanceOf(RedirectView.class, modelAndView.getView(), "Expected RedirectView");
        assertTrue(modelAndView.getModel().isEmpty());

        val redirectUrl = ((AbstractUrlBasedView) modelAndView.getView()).getUrl();
        val params = splitQuery(redirectUrl.substring(redirectUrl.indexOf('#') + 1));

        verifyParam(params, OAuth20Constants.STATE, STATE);
        verifyParam(params, OAuth20Constants.NONCE, NONCE);
    }
}
