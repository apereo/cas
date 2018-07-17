package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
    private OAuthRegisteredService registeredService;

    @Before
    public void before() {
        val service = RegisteredServiceTestUtils.getService();

        val serviceManager = mock(ServicesManager.class);
        registeredService = new OAuthRegisteredService();
        registeredService.setName("OAuth");
        registeredService.setClientId("client");
        registeredService.setClientSecret("secret");
        registeredService.setServiceId(service.getId());

        when(serviceManager.getAllServices()).thenReturn(CollectionUtils.wrapList(registeredService));

        this.validator = new OAuth20PasswordGrantTypeTokenRequestValidator(new RegisteredServiceAccessStrategyAuditableEnforcer(),
            serviceManager, new WebApplicationServiceFactory());
    }

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(this.validator.validate(new J2EContext(request, response)));

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("client");
        val session = request.getSession(true);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        request.setParameter(OAuth20Constants.GRANT_TYPE, getGrantType().getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        assertTrue(this.validator.validate(new J2EContext(request, response)));
    }

    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }
}
