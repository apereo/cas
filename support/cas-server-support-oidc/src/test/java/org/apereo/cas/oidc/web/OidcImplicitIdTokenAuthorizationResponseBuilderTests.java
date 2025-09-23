package org.apereo.cas.oidc.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcImplicitIdTokenAuthorizationResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
class OidcImplicitIdTokenAuthorizationResponseBuilderTests extends AbstractOidcTests {
    @Test
    void verifyOperation() {
        val authzRequest = OAuth20AuthorizationRequest
            .builder()
            .responseType(OAuth20ResponseTypes.ID_TOKEN.getType())
            .build();
        assertTrue(oidcImplicitIdTokenCallbackUrlBuilder.supports(authzRequest));
    }

    @ParameterizedTest
    @MethodSource("getResponseModes")
    void verifyBuild(final OAuth20ResponseModeTypes responseMode) throws Throwable {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, List.of("state"));
        attributes.put(OAuth20Constants.NONCE, List.of("nonce"));

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal, attributes);
        val code = addCode(principal, registeredService);

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        val tokenRequestContext = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.ID_TOKEN)
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .token(code)
            .responseMode(responseMode)
            .userProfile(profile)
            .redirectUri("https://oauth.example.org")
            .scopes(code.getScopes())
            .build();

        servicesManager.save(registeredService);
        val modelAndView = oidcImplicitIdTokenCallbackUrlBuilder.build(tokenRequestContext);
        assertNotNull(modelAndView);
        if (responseMode == null || responseMode == OAuth20ResponseModeTypes.FRAGMENT) {
            val redirectUrl = ((AbstractUrlBasedView) modelAndView.getView()).getUrl();
            assertNotNull(redirectUrl);
            val urlBuilder = new URIBuilder(redirectUrl);
            assertTrue(urlBuilder.getQueryParams().isEmpty());
            val fragment = urlBuilder.getFragment();
            assertTrue(fragment.contains(OidcConstants.ID_TOKEN + '='));
            assertTrue(fragment.contains(OAuth20Constants.STATE + '='));
            assertTrue(fragment.contains(OAuth20Constants.NONCE + '='));
            assertFalse(fragment.contains(OAuth20Constants.ACCESS_TOKEN + '='));
        }
        if (responseMode == OAuth20ResponseModeTypes.FORM_POST) {
            val redirectUrl = modelAndView.getModelMap().get("originalUrl").toString();
            val parameters = (Map) modelAndView.getModelMap().get("parameters");
            assertNotNull(redirectUrl);
            val urlBuilder = new URIBuilder(redirectUrl);
            assertNull(urlBuilder.getFragment());
            assertTrue(urlBuilder.getQueryParams().isEmpty());
            assertTrue(parameters.containsKey(OidcConstants.ID_TOKEN));
            assertTrue(parameters.containsKey(OAuth20Constants.STATE));
            assertTrue(parameters.containsKey(OAuth20Constants.NONCE));
            assertFalse(parameters.containsKey(OAuth20Constants.ACCESS_TOKEN));
        }
    }

    private static Stream<Arguments> getResponseModes() {
        return Stream.of(
            Arguments.of(OAuth20ResponseModeTypes.FRAGMENT),
            Arguments.of((Object) null),
            Arguments.of(OAuth20ResponseModeTypes.FORM_POST)
        );
    }
}
