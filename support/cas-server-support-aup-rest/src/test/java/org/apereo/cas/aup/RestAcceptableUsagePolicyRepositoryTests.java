package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
public class RestAcceptableUsagePolicyRepositoryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verify() {
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        val props = new AcceptableUsagePolicyProperties();
        props.getRest().setUrl("http://localhost:9298");
        props.setAupAttributeName("givenName");
        val r = new RestAcceptableUsagePolicyRepository(ticketRegistrySupport, props);

        val data = StringUtils.EMPTY;
        try (val webServer = new MockWebServer(9298,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertFalse(r.isUsagePolicyAcceptedBy(CoreAuthenticationTestUtils.getPrincipal()));

            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertTrue(r.submit(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyFetch() throws Exception {
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        val props = new AcceptableUsagePolicyProperties();
        props.getRest().setUrl("http://localhost:9198");
        props.setAupAttributeName("givenName");
        val r = new RestAcceptableUsagePolicyRepository(ticketRegistrySupport, props);

        val input = AcceptableUsagePolicyTerms.builder()
            .code("example")
            .defaultText("hello world")
            .build();
        val data = MAPPER.writeValueAsString(input);
        try (val webServer = new MockWebServer(9198,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            val terms = r.fetchPolicy(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            assertTrue(terms.isPresent());
            assertEquals(terms.get(), input);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
