package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenResponseGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
class OidcAccessTokenResponseGeneratorTests extends AbstractOidcTests {
    @Test
    void verifyAccessTokenResponseAsCode() throws Throwable {
        val token = OAuth20TokenGeneratedResult
            .builder()
            .accessToken(getAccessToken())
            .refreshToken(getRefreshToken())
            .registeredService(getOidcRegisteredService())
            .responseType(OAuth20ResponseTypes.CODE)
            .build();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, new JEESessionStore());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);

        val result = OAuth20AccessTokenResponseResult.builder()
            .service(RegisteredServiceTestUtils.getService())
            .registeredService(getOidcRegisteredService())
            .casProperties(casProperties)
            .generatedToken(token)
            .userProfile(profile)
            .build();

        val modelAndView = oidcAccessTokenResponseGenerator.generate(result);
        assertNotNull(modelAndView);
        val modelMap = modelAndView.getModelMap();
        assertTrue(modelMap.containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(modelMap.containsKey(OAuth20Constants.SCOPE));
        assertTrue(modelMap.containsKey(OAuth20Constants.EXPIRES_IN));
        assertTrue(modelMap.containsKey(OAuth20Constants.TOKEN_TYPE));
    }

    @Test
    void verifyAccessTokenResponseForDeviceCode() throws Throwable {
        val devCode = deviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService());

        val token = OAuth20TokenGeneratedResult.builder()
            .registeredService(getOidcRegisteredService())
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(devCode.getId())
            .userCode(deviceUserCodeFactory.createDeviceUserCode(devCode.getService()).getId())
            .build();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, new JEESessionStore());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);

        val result = OAuth20AccessTokenResponseResult.builder()
            .service(RegisteredServiceTestUtils.getService())
            .registeredService(getOidcRegisteredService())
            .casProperties(casProperties)
            .generatedToken(token)
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .userProfile(profile)
            .build();

        val mv = oidcAccessTokenResponseGenerator.generate(result);
        assertNotNull(mv);
        val modelMap = mv.getModelMap();

        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_VERIFICATION_URI));
        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_USER_CODE));
        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_CODE));
        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_INTERVAL));
    }
}
