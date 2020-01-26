package org.apereo.cas.support.oauth.web.response.callback;

import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenExpirationPolicyBuilder;
import org.jasig.cas.client.util.URIBuilder;
import org.junit.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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

/**
 * This is {@link OAuth20TokenAuthorizationResponseBuilderTest}.
 *
 * @author David Albrecht
 * @since 6.1.3
 */
@Tag("OAuth")
public class OAuth20TokenAuthorizationResponseBuilderTest extends AbstractOAuth20Tests {

    private static final String STATE = "%123=";
    private static final String NONCE = "%123=";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyUnchangedStateAndNonceParameter() throws Exception {

        val registeredService = getRegisteredService("example", CLIENT_SECRET, new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val service = RegisteredServiceTestUtils.getService("example");
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, Collections.singletonList(STATE));
        attributes.put(OAuth20Constants.NONCE, Collections.singletonList(NONCE));

        val holder = AccessTokenRequestDataHolder
            .builder()
            .clientId(registeredService.getClientId())
            .service(service)
            .authentication(RegisteredServiceTestUtils.getAuthentication(RegisteredServiceTestUtils.getPrincipal(ID), attributes))
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.NONE)
            .responseType(OAuth20ResponseTypes.TOKEN)
            .ticketGrantingTicket(new MockTicketGrantingTicket(ID))
            .build();
        val generatedToken = oauthTokenGenerator.generate(holder);

        if (generatedToken
            .getAccessToken()
            .isEmpty()) {
            Assert.fail("Expected access token");
        }

        final OAuth20AccessToken oAuth20AccessToken = generatedToken
            .getAccessToken()
            .get();

        val tokenExpirationPolicyBuilder = new OAuth20AccessTokenExpirationPolicyBuilder(casProperties);
        val tokenAuthorizationResponseBuilder = new OAuth20TokenAuthorizationResponseBuilder(oauthTokenGenerator,
                                                                                             tokenExpirationPolicyBuilder,
                                                                                             servicesManager,
                                                                                             accessTokenJwtBuilder);

        val modelAndView = tokenAuthorizationResponseBuilder.buildCallbackUrlResponseType(holder,
                                                                                          REDIRECT_URI,
                                                                                          oAuth20AccessToken,
                                                                                          Collections.emptyList(),
                                                                                          null,
                                                                                          new JEEContext(new MockHttpServletRequest(),
                                                                                                         new MockHttpServletResponse()));

        Assert.assertTrue("Expected RedirectView", modelAndView.getView() instanceof RedirectView);

        val redirectUrl = ((RedirectView) modelAndView.getView()).getUrl();
        val params = splitQuery(new URIBuilder(redirectUrl).getFragment());

        verifyParam(params, OAuth20Constants.STATE, STATE);
        verifyParam(params, OAuth20Constants.NONCE, NONCE);
    }

    private void verifyParam(Map<String, List<String>> params, String paramName, String expectedParamValue) {
        Assert.assertTrue("Expected " + paramName + "  param in redirect URL", params.containsKey(paramName));
        Assert.assertEquals("Expected one value for " + paramName + " param",
                            1,
                            params
                                .get(paramName)
                                .size());
        Assert.assertEquals("Expected unchanged " + paramName + "  param",
                            expectedParamValue,
                            params
                                .get(paramName)
                                .get(0));
    }

    private Map<String, List<String>> splitQuery(String fragment) {
        if (StringUtils.isBlank(fragment)) {
            return Collections.emptyMap();
        }
        return Arrays
            .stream(fragment.split("&"))
            .map(this::splitQueryParameter)
            .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey,
                                           LinkedHashMap::new,
                                           Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

}
