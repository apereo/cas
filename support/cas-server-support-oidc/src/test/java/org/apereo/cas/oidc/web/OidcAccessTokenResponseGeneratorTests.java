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
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
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
public class OidcAccessTokenResponseGeneratorTests extends AbstractOidcTests {
    @Test
    public void verifyAccessTokenResponseAsCode() {
        val token = OAuth20TokenGeneratedResult.builder()
            .accessToken(getAccessToken())
            .refreshToken(getRefreshToken())
            .registeredService(getOidcRegisteredService())
            .responseType(OAuth20ResponseTypes.CODE)
            .build();

        val result = OAuth20AccessTokenResponseResult.builder()
            .service(RegisteredServiceTestUtils.getService())
            .registeredService(getOidcRegisteredService())
            .resourceLoader(resourceLoader)
            .casProperties(casProperties)
            .generatedToken(token)
            .build();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");
        
        manager.save(true, profile, false);
        val mv = oidcAccessTokenResponseGenerator.generate(request, response, result);
        assertNotNull(mv);
        val modelMap = mv.getModelMap();
        assertTrue(modelMap.containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(modelMap.containsKey(OAuth20Constants.SCOPE));
        assertTrue(modelMap.containsKey(OAuth20Constants.EXPIRES_IN));
        assertTrue(modelMap.containsKey(OAuth20Constants.TOKEN_TYPE));
    }

    @Test
    public void verifyAccessTokenResponseForDeviceCode() {
        val devCode = deviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService());

        val token = OAuth20TokenGeneratedResult.builder()
            .registeredService(getOidcRegisteredService())
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(devCode.getId())
            .userCode(deviceTokenFactory.createDeviceUserCode(devCode).getId())
            .build();

        val result = OAuth20AccessTokenResponseResult.builder()
            .service(RegisteredServiceTestUtils.getService())
            .registeredService(getOidcRegisteredService())
            .resourceLoader(resourceLoader)
            .casProperties(casProperties)
            .generatedToken(token)
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .build();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);
        val mv = oidcAccessTokenResponseGenerator.generate(request, response, result);
        assertNotNull(mv);
        val modelMap = mv.getModelMap();
        
        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_VERIFICATION_URI));
        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_USER_CODE));
        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_CODE));
        assertTrue(modelMap.containsKey(OAuth20Constants.DEVICE_INTERVAL));
    }
}
