package org.apereo.cas.acct;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccountRegistrationRequestAuditPrincipalIdResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Audits")
public class AccountRegistrationRequestAuditPrincipalIdResolverTests {
    @Test
    public void verifySupports() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
        AccountRegistrationUtils.putAccountRegistrationRequest(context, registrationRequest);

        val accountService = mock(AccountRegistrationService.class);
        val resolver = new AccountRegistrationRequestAuditPrincipalIdResolver(accountService);
        assertTrue(resolver.supports(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), null, null));
        assertTrue(resolver.supports(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), registrationRequest, null));
    }

    @Test
    public void verifyPrincipalId() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
        AccountRegistrationUtils.putAccountRegistrationRequestUsername(context, "casuser");
        AccountRegistrationUtils.putAccountRegistrationRequest(context, registrationRequest);

        val accountService = mock(AccountRegistrationService.class);
        val resolver = new AccountRegistrationRequestAuditPrincipalIdResolver(accountService);
        assertNotNull(resolver.getPrincipalIdFrom(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), null, null));
        assertNotNull(resolver.getPrincipalIdFrom(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), registrationRequest, null));
    }
}
