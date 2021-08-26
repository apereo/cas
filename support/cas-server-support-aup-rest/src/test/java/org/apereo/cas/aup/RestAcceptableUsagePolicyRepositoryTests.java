package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verify() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);

        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        val props = new AcceptableUsagePolicyProperties();
        props.getRest().setUrl("http://localhost:9298");
        props.getCore().setAupAttributeName("givenName");
        val r = new RestAcceptableUsagePolicyRepository(ticketRegistrySupport, props);

        val data = StringUtils.EMPTY;
        try (val webServer = new MockWebServer(9298,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertFalse(r.isUsagePolicyAcceptedBy(CoreAuthenticationTestUtils.getPrincipal()));
            assertTrue(r.submit(context));
        }
    }

    @Test
    public void verifyFails() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        val props = new AcceptableUsagePolicyProperties();
        props.getRest().setUrl("http://localhost:9299");
        props.getCore().setAupAttributeName("givenName");
        val r = new RestAcceptableUsagePolicyRepository(ticketRegistrySupport, props);

        val data = StringUtils.EMPTY;
        try (val webServer = new MockWebServer(9299,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.SERVICE_UNAVAILABLE)) {
            webServer.start();
            assertFalse(r.fetchPolicy(context).isPresent());
            assertFalse(r.submit(context));
        }
    }

    @Test
    public void verifyFetch() throws Exception {
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        val props = new AcceptableUsagePolicyProperties();
        props.getRest().setUrl("http://localhost:9198");
        props.getCore().setAupAttributeName("givenName");
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
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            val terms = r.fetchPolicy(context);
            assertTrue(terms.isPresent());
            assertEquals(terms.get(), input);
        }
    }
}
