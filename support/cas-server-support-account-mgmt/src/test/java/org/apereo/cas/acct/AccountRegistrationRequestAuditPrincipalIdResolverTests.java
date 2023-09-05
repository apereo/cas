package org.apereo.cas.acct;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class AccountRegistrationRequestAuditPrincipalIdResolverTests {
    @Test
    void verifySupports() throws Throwable {
        val context = MockRequestContext.create();

        val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
        AccountRegistrationUtils.putAccountRegistrationRequest(context, registrationRequest);

        val accountService = mock(AccountRegistrationService.class);
        val resolver = new AccountRegistrationRequestAuditPrincipalIdResolver(accountService);
        assertTrue(resolver.supports(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), null, null));
        assertTrue(resolver.supports(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), registrationRequest, null));
    }

    @Test
    void verifyPrincipalId() throws Throwable {
        val context = MockRequestContext.create();

        val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
        AccountRegistrationUtils.putAccountRegistrationRequestUsername(context, "casuser");
        AccountRegistrationUtils.putAccountRegistrationRequest(context, registrationRequest);

        val accountService = mock(AccountRegistrationService.class);
        val resolver = new AccountRegistrationRequestAuditPrincipalIdResolver(accountService);
        assertNotNull(resolver.getPrincipalIdFrom(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), null, null));
        assertNotNull(resolver.getPrincipalIdFrom(mock(JoinPoint.class), RegisteredServiceTestUtils.getAuthentication(), registrationRequest, null));
    }
}
