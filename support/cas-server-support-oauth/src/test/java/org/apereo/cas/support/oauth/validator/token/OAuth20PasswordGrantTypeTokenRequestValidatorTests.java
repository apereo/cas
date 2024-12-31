package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

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
        val context = new JEEContext(request, response);
        assertFalse(validator.validate(context));

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(supportingService.getClientId());
        
        val session = storeProfileIntoSession(request, profile);

        request.setParameter(OAuth20Constants.GRANT_TYPE, getGrantType().getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, supportingService.getClientId());
        assertTrue(validator.validate(context));

        request.setParameter(OAuth20Constants.CLIENT_ID, nonSupportingService.getClientId());
        profile.setId(nonSupportingService.getClientId());
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertFalse(validator.validate(context));

        request.setParameter(OAuth20Constants.CLIENT_ID, promiscuousService.getClientId());
        profile.setId(promiscuousService.getClientId());
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertTrue(validator.validate(context));
    }

    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    protected OAuth20GrantTypes getWrongGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }
}
