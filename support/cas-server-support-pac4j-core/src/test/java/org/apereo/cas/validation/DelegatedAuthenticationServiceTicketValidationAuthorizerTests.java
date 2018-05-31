package org.apereo.cas.validation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationServiceTicketValidationAuthorizerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DelegatedAuthenticationServiceTicketValidationAuthorizerTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyAction() {
        final var servicesManager = mock(ServicesManager.class);
        final var registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        final var policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(CollectionUtils.wrapList("SomeClient"));
        when(registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy()).thenReturn(policy);

        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        final var assertion = mock(Assertion.class);
        final var principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, "CasClient"));
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal, principal.getAttributes()));

        final var az = new DelegatedAuthenticationServiceTicketValidationAuthorizer(servicesManager,
            new RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer());
        thrown.expect(UnauthorizedServiceException.class);
        az.authorize(new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService(), assertion);
    }
}
