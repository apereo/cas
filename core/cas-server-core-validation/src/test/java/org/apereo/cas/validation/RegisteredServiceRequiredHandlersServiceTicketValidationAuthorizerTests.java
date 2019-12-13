package org.apereo.cas.validation;

import org.apereo.cas.TestOneTimePasswordAuthenticationHandler;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasCoreServicesConfiguration.class
})
public class RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizerTests {
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Test
    public void verifyOperationWithNoHandlers() {
        val authz = new RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizer(servicesManager);
        val assertion = mock(Assertion.class);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
            Map.of(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, List.of()));
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        assertThrows(UnauthorizedServiceException.class, () -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    public void verifyOperationWithMismatchedHandlers() {
        val authz = new RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizer(servicesManager);
        val assertion = mock(Assertion.class);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
            Map.of(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, List.of("SampleHandler")));
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        assertThrows(UnauthorizedServiceException.class, () -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    public void verifyOperationWithHandlers() {
        val authz = new RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizer(servicesManager);
        val assertion = mock(Assertion.class);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
            Map.of(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS,
                List.of(AcceptUsersAuthenticationHandler.class.getSimpleName(),
                    TestOneTimePasswordAuthenticationHandler.class.getSimpleName())));
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                authz.authorize(new MockHttpServletRequest(), service, assertion);
            }
        });
    }
}
