package org.apereo.cas.validation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationServiceTicketValidationAuthorizerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DelegatedAuthenticationServiceTicketValidationAuthorizerTests {

    @Test
    public void verifyAction() {
        val servicesManager = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(CollectionUtils.wrapList("SomeClient"));
        when(registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy()).thenReturn(policy);

        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        val assertion = mock(Assertion.class);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, "CasClient"));
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(principal, principal.getAttributes()));

        val az = new DelegatedAuthenticationServiceTicketValidationAuthorizer(servicesManager,
            new RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer());
        assertThrows(UnauthorizedServiceException.class, () -> {
            az.authorize(new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService(), assertion);
        });
    }
}
