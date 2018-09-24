package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    @Test
    public void verifyAction() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val support = mock(TicketRegistrySupport.class);
        when(support.getAuthenticatedPrincipalFrom(anyString()))
            .thenReturn(CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("carLicense", "false")));
        val repo = new DefaultAcceptableUsagePolicyRepository(support);

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-12345");

        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casaup");
        assertFalse(repo.verify(context, c).getLeft());
        assertTrue(repo.submit(context, c));
        assertTrue(repo.verify(context, c).getLeft());
    }

    @Override
    public boolean hasLiveUpdates() {
        return true;
    }
}
