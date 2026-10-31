package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
@TestPropertySource(properties = "cas.authn.oauth.session-replication.replicate-sessions=false")
class OAuth20PasswordGrantTypeTokenRequestValidatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthPasswordGrantTypeTokenRequestValidator")
    private OAuth20TokenRequestValidator validator;

    private OAuthRegisteredService supportingService;

    private OAuthRegisteredService nonSupportingService;

    private OAuthRegisteredService promiscuousService;

    @BeforeEach
    void before() {
        supportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(getGrantType()));
        nonSupportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL2,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(getWrongGrantType()));
        promiscuousService = RequestValidatorTestUtils.getPromiscuousService(
            RegisteredServiceTestUtils.CONST_TEST_URL3,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            RequestValidatorTestUtils.SHARED_SECRET);
        servicesManager.save(supportingService, nonSupportingService, promiscuousService);
    }

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(validator.validate(request, response));

        val profile = new DefaultOAuth20UserProfile(supportingService.getClientId());
        
        storeProfileIntoSession(request, profile);

        request.setParameter(OAuth20Constants.GRANT_TYPE, getGrantType().getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, supportingService.getClientId());
        assertTrue(validator.validate(request, response));

        request.setParameter(OAuth20Constants.CLIENT_ID, nonSupportingService.getClientId());
        profile.setId(nonSupportingService.getClientId());
        storeProfileIntoSession(request, profile);
        assertFalse(validator.validate(request, response));

        request.setParameter(OAuth20Constants.CLIENT_ID, promiscuousService.getClientId());
        profile.setId(promiscuousService.getClientId());
        storeProfileIntoSession(request, profile);
        assertTrue(validator.validate(request, response));
    }

    @Test
    void verifyRequestedResources() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val profile = new DefaultOAuth20UserProfile(supportingService.getClientId());
        storeProfileIntoSession(request, profile);

        val allowedResource = "https://api.example.org/";
        supportingService.setAllowedResources(Set.of(allowedResource));
        servicesManager.save(supportingService);
        request.setParameter(OAuth20Constants.GRANT_TYPE, getGrantType().getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, supportingService.getClientId());

        request.setParameter(OAuth20Constants.RESOURCE, allowedResource);
        assertTrue(validator.validate(request, response));

        request.setParameter(OAuth20Constants.RESOURCE, "https://other.example.org/");
        assertThrows(OAuth20InvalidTargetException.class, () -> validator.validate(request, response));

        request.setParameter(OAuth20Constants.RESOURCE, allowedResource + "#fragment");
        assertThrows(OAuth20InvalidTargetException.class, () -> validator.validate(request, response));
    }

    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    protected OAuth20GrantTypes getWrongGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }
}
