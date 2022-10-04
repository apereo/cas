package org.apereo.cas.web.flow.delegation;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.BasePasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Optional;

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
public class PasswordlessDelegatedClientAuthenticationWebflowStateContributorTests
    extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("passwordlessDelegatedClientAuthenticationWebflowStateContributor")
    private DelegatedClientAuthenticationWebflowStateContributor contributor;

    @Test
    public void verifyStore() throws Exception {
        val client = new CasClient();
        val context = new MockRequestContext();
        val account = PasswordlessUserAccount.builder().username("casuser").build();
        WebUtils.putPasswordlessAuthenticationAccount(context, account);
        val webContext = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val stored = contributor.store(context, webContext, client);
        assertTrue(stored.containsKey(PasswordlessUserAccount.class.getName()));
    }

    @Test
    public void verifyRestore() throws Exception {
        val client = new CasClient();
        val context = new MockRequestContext();
        val account = PasswordlessUserAccount.builder().username("casuser").build();
        val sessionTicket = mock(TransientSessionTicket.class);
        val service = RegisteredServiceTestUtils.getService();
        when(sessionTicket.getService()).thenReturn(service);
        when(sessionTicket.getProperty(anyString(), any())).thenReturn(account);

        val stored = contributor.restore(context,
            new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse()),
            Optional.of(sessionTicket), client);
        assertEquals(stored, service);
        assertNotNull(WebUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class));
    }

    @Test
    public void verifyRestoreWithoutSessionTicket() throws Exception {
        val client = new CasClient();
        val context = new MockRequestContext();
        val webContext = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val stored = contributor.restore(context, webContext, Optional.empty(), client);
        assertNull(stored);
        assertNull(WebUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class));
    }
}
