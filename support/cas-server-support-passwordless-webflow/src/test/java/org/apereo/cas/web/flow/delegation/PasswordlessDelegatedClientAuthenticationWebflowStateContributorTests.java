package org.apereo.cas.web.flow.delegation;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BasePasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordlessDelegatedClientAuthenticationWebflowStateContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
class PasswordlessDelegatedClientAuthenticationWebflowStateContributorTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("passwordlessDelegatedClientAuthenticationWebflowStateContributor")
    private DelegatedClientAuthenticationWebflowStateContributor contributor;

    @Test
    void verifyStore() throws Throwable {
        val client = new FormClient();
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder().username("casuser").build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);

        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username("casuser").build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(context, passwordlessRequest);

        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val stored = contributor.store(context, webContext, client);
        assertTrue(stored.containsKey(PasswordlessUserAccount.class.getName()));
        assertTrue(stored.containsKey(PasswordlessAuthenticationRequest.class.getName()));
    }

    @Test
    void verifyRestore() throws Throwable {
        val client = new FormClient();
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder().username("casuser").build();
        val sessionTicket = mock(TransientSessionTicket.class);
        val service = RegisteredServiceTestUtils.getService();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username("casuser").build();

        when(sessionTicket.getService()).thenReturn(service);
        when(sessionTicket.getProperty(ArgumentMatchers.eq(PasswordlessUserAccount.class.getName()), any())).thenReturn(account);
        when(sessionTicket.getProperty(ArgumentMatchers.eq(PasswordlessAuthenticationRequest.class.getName()), any())).thenReturn(passwordlessRequest);

        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val stored = contributor.restore(context, webContext, Optional.of(sessionTicket), client);
        assertEquals(stored, service);
        assertNotNull(PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class));
    }

    @Test
    void verifyRestoreWithoutSessionTicket() throws Throwable {
        val client = new FormClient();
        val context = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val stored = contributor.restore(context, webContext, Optional.empty(), client);
        assertNull(stored);
        assertNull(PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class));
    }
}
