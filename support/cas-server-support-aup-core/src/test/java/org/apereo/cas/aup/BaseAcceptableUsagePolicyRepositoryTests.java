package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public abstract class BaseAcceptableUsagePolicyRepositoryTests {

    protected TicketRegistrySupport mockSupport;
    protected MockRequestContext context;
    protected MockHttpServletRequest request;

    protected abstract AcceptableUsagePolicyRepository getRepository();

    @Before
    public final void initializeTest() {
        context = new MockRequestContext();
        request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        mockSupport = mock(TicketRegistrySupport.class);
        when(mockSupport.getAuthenticatedPrincipalFrom(anyString()))
            .thenReturn(CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("carLicense", "false")));
    }

    @Test
    public void verifyAction() {
        val repo = getRepository();

        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-12345");

        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casaup");
        assertFalse(repo.verify(context, c).getLeft());
        assertTrue(repo.submit(context, c));
        assertTrue(repo.verify(context, c).getLeft());
    }
}
