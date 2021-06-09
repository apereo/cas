package org.apereo.cas.aup;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.model.support.aup.InMemoryAcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Tag("Simple")
public class DefaultAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    private static void verifyAction(final AcceptableUsagePolicyProperties properties) {
        val context = getRequestContext();

        val repo = getRepositoryInstance(properties);

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-12345");

        assertFalse(repo.verify(context).isAccepted());
        assertTrue(repo.submit(context));
        assertTrue(repo.verify(context).isAccepted());
    }

    private static MockRequestContext getRequestContext() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        return context;
    }

    private static AcceptableUsagePolicyRepository getRepositoryInstance(final AcceptableUsagePolicyProperties properties) {
        val support = mock(TicketRegistrySupport.class);
        when(support.getAuthenticatedPrincipalFrom(anyString()))
            .thenReturn(CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("carLicense", "false")));
        return new DefaultAcceptableUsagePolicyRepository(support, properties);
    }

    @Test
    public void verifyActionDefaultGlobal() {
        val properties = new AcceptableUsagePolicyProperties();
        properties.getInMemory().setScope(InMemoryAcceptableUsagePolicyProperties.Scope.GLOBAL);
        verifyAction(properties);
    }

    @Test
    public void verifyActionDefaultAuthentication() {
        val properties = new AcceptableUsagePolicyProperties();
        properties.getInMemory().setScope(InMemoryAcceptableUsagePolicyProperties.Scope.AUTHENTICATION);
        verifyAction(properties);
    }

    @Test
    public void verifyActionNoAuthentication() {
        val properties = new AcceptableUsagePolicyProperties();
        properties.getInMemory().setScope(InMemoryAcceptableUsagePolicyProperties.Scope.AUTHENTICATION);
        val context = getRequestContext();
        val repo = getRepositoryInstance(properties);
        assertThrows(AuthenticationException.class, () -> repo.verify(context));
    }

    @Test
    public void verifyProps() {
        val status = AcceptableUsagePolicyStatus.accepted(CoreAuthenticationTestUtils.getPrincipal());
        status.clearProperties();
        status.addProperty("example", "cas");
        status.setProperty("example2", "cas");
        status.addProperty("example2", "system");
        status.setProperty("user", "casuser");
        assertEquals(List.of("cas"), status.getPropertyOrDefault("example", "hello"));
        assertEquals(List.of("casuser"), status.getProperty("user"));
        assertEquals(List.of("cas", "system"), status.getPropertyOrDefault("example2", List.of()));
        assertEquals(Set.of("hello"), status.getPropertyOrDefault("nada", "hello"));


        assertEquals(List.of("hello1", "hello2"), status.getPropertyOrDefault("nada", "hello1", "hello2"));
        assertEquals(List.of("cas", "system"), status.getPropertyOrDefault("example2", "hello1", "hello2"));
    }

    @Override
    public boolean hasLiveUpdates() {
        return true;
    }
}
