package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyRestConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.support.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import java.util.List;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = {
    CasAcceptableUsagePolicyRestConfiguration.class,
    BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.acceptable-usage-policy.rest.url=http://localhost:9836",
    "cas.acceptable-usage-policy.core.aup-attribute-name=givenName"
})
class RestAcceptableUsagePolicyRepositoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();
    private static final int PORT = 9836;

    @Autowired
    @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;
    
    @Test
    void verify() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.GERMAN));
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        try (val webServer = new MockWebServer(PORT, StringUtils.EMPTY, HttpStatus.BAD_REQUEST)) {
            webServer.start();
            assertFalse(acceptableUsagePolicyRepository.verify(context).isAccepted());
        }
        try (val webServer = new MockWebServer(PORT, StringUtils.EMPTY, HttpStatus.ACCEPTED)) {
            webServer.start();
            assertTrue(acceptableUsagePolicyRepository.submit(context));
        }
    }

    @Test
    void verifyFails() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.GERMAN));
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        try (val webServer = new MockWebServer(PORT, StringUtils.EMPTY, HttpStatus.BAD_REQUEST)) {
            webServer.start();
            assertFalse(acceptableUsagePolicyRepository.fetchPolicy(context).isPresent());
            assertFalse(acceptableUsagePolicyRepository.submit(context));
        }
    }

    @Test
    void verifyFetch() throws Throwable {
        val input = AcceptableUsagePolicyTerms.builder()
            .code("example")
            .defaultText("hello world")
            .build();
        val data = MAPPER.writeValueAsString(input);
        try (val webServer = new MockWebServer(PORT, data)) {
            webServer.start();
            val context = new MockRequestContext();
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            val terms = acceptableUsagePolicyRepository.fetchPolicy(context);
            assertTrue(terms.isPresent());
            assertEquals(terms.get(), input);
        }
    }
}
