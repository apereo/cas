package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockWebServer;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RestAcceptableUsagePolicyRepositoryTests {
    @Test
    public void verify() {
        final var ticketRegistrySupport = mock(TicketRegistrySupport.class);
        final var props = new AcceptableUsagePolicyProperties.Rest();
        props.setUrl("http://localhost:9298");
        final var r = new RestAcceptableUsagePolicyRepository(ticketRegistrySupport, "givenName", props);

        final var data = "";
        try (var webServer = new MockWebServer(9298,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertFalse(r.isUsagePolicyAcceptedBy(CoreAuthenticationTestUtils.getPrincipal()));

            final var context = new MockRequestContext();
            final var request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertTrue(r.submit(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
