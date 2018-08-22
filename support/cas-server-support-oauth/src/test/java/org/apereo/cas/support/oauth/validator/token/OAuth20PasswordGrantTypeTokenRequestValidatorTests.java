package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20PasswordGrantTypeTokenRequestValidatorTests {
    private OAuth20TokenRequestValidator validator;
    private OAuthRegisteredService supportingService;
    private OAuthRegisteredService nonSupportingService;
    private OAuthRegisteredService promiscuousService;

    @Before
    public void before() {
        final Service service = RegisteredServiceTestUtils.getService();

        final ServicesManager serviceManager = mock(ServicesManager.class);
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
        promiscuousService = RequestValidatorTestUtils.getPromiscousService(
                RegisteredServiceTestUtils.CONST_TEST_URL3,
                RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
                RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET);

        when(serviceManager.getAllServices()).thenReturn(CollectionUtils.wrapList(
                supportingService, nonSupportingService, promiscuousService));

        this.validator = new OAuth20PasswordGrantTypeTokenRequestValidator(new RegisteredServiceAccessStrategyAuditableEnforcer(),
            serviceManager, new WebApplicationServiceFactory());
    }

    @Test
    public void verifyOperation() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(this.validator.validate(new J2EContext(request, response)));

        final CommonProfile profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        final HttpSession session = request.getSession(true);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        request.setParameter(OAuth20Constants.GRANT_TYPE, getGrantType().getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, supportingService.getClientId());
        assertTrue(this.validator.validate(new J2EContext(request, response)));

        request.setParameter(OAuth20Constants.CLIENT_ID, nonSupportingService.getClientId());
        profile.setId(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);
        assertFalse(this.validator.validate(new J2EContext(request, response)));

        request.setParameter(OAuth20Constants.CLIENT_ID, promiscuousService.getClientId());
        profile.setId(RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);
        assertTrue(this.validator.validate(new J2EContext(request, response)));
    }

    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    protected OAuth20GrantTypes getWrongGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }
}
