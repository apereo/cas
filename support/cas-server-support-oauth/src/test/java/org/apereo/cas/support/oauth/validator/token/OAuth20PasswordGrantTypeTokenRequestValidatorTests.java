package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
public class OAuth20PasswordGrantTypeTokenRequestValidatorTests {
    private OAuth20TokenRequestValidator validator;
    private OAuthRegisteredService supportingService;
    private OAuthRegisteredService nonSupportingService;
    private OAuthRegisteredService promiscuousService;

    @BeforeEach
    public void before() {
        val serviceManager = mock(ServicesManager.class);
        supportingService = RequestValidatorTestUtils.getService(
                RegisteredServiceTestUtils.CONST_TEST_URL,
                RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET,
                CollectionUtils.wrapSet(getGrantType()));
        nonSupportingService = RequestValidatorTestUtils.getService(
                RegisteredServiceTestUtils.CONST_TEST_URL2,
                RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET,
                CollectionUtils.wrapSet(getWrongGrantType()));
        promiscuousService = RequestValidatorTestUtils.getPromiscuousService(
                RegisteredServiceTestUtils.CONST_TEST_URL3,
                RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
                RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET);

        when(serviceManager.getAllServices()).thenReturn(CollectionUtils.wrapList(
                supportingService, nonSupportingService, promiscuousService));

        val context = OAuth20ConfigurationContext.builder()
            .servicesManager(serviceManager)
            .webApplicationServiceServiceFactory(new WebApplicationServiceFactory())
            .registeredServiceAccessStrategyEnforcer(new RegisteredServiceAccessStrategyAuditableEnforcer())
            .sessionStore(new JEESessionStore())
            .build();
        this.validator = new OAuth20PasswordGrantTypeTokenRequestValidator(context);
    }

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(this.validator.validate(new JEEContext(request, response)));

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        val session = request.getSession(true);
        assertNotNull(session);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        request.setParameter(OAuth20Constants.GRANT_TYPE, getGrantType().getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, supportingService.getClientId());
        assertTrue(this.validator.validate(new JEEContext(request, response)));

        request.setParameter(OAuth20Constants.CLIENT_ID, nonSupportingService.getClientId());
        profile.setId(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertFalse(this.validator.validate(new JEEContext(request, response)));

        request.setParameter(OAuth20Constants.CLIENT_ID, promiscuousService.getClientId());
        profile.setId(RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertTrue(this.validator.validate(new JEEContext(request, response)));
    }

    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    protected OAuth20GrantTypes getWrongGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }
}
