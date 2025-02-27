package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.RestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApiAuthentication")
class RestMultifactorAuthenticationProviderBypassEvaluatorTests {
    private StaticApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
    }

    @Test
    void verifyOperationShouldProceed() {
        try (val webServer = new MockWebServer("Y", HttpStatus.ACCEPTED)) {
            webServer.start();
            val props = new MultifactorAuthenticationProviderBypassProperties();
            props.getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val provider = new TestMultifactorAuthenticationProvider();
            val evaluator = new RestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId(), applicationContext);
            val res = evaluator.shouldMultifactorAuthenticationProviderExecute(MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
                MultifactorAuthenticationTestUtils.getRegisteredService(), provider,
                new MockHttpServletRequest(), MultifactorAuthenticationTestUtils.getService("service"));
            assertTrue(res);
        }
    }

    @Test
    void verifyOperationFailsWithNoProvider() {
        try (val webServer = new MockWebServer("Y", HttpStatus.ACCEPTED)) {
            webServer.start();
            val props = new MultifactorAuthenticationProviderBypassProperties();
            props.getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val provider = new TestMultifactorAuthenticationProvider();
            val evaluator = new RestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId(), applicationContext);
            val res = evaluator.shouldMultifactorAuthenticationProviderExecute(MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
                MultifactorAuthenticationTestUtils.getRegisteredService(), null,
                new MockHttpServletRequest(), MultifactorAuthenticationTestUtils.getService("service"));
            assertTrue(res);
        }
    }

    @Test
    void verifyRestSendsQueryParametersAndHeaders() throws Throwable {
        try (val webServer = new okhttp3.mockwebserver.MockWebServer()) {
            val port = webServer.getPort();
            val response = new MockResponse().setResponseCode(HttpStatus.ACCEPTED.value());
            webServer.enqueue(response);

            val props = new MultifactorAuthenticationProviderBypassProperties();
            props.getRest().setUrl("http://localhost:" + port);
            props.getRest().setHeaders(Map.of(
                    "X-Custom-Header", "HeaderValue",
                    "Authorization", "Bearer token"
            ));
            val provider = new TestMultifactorAuthenticationProvider();
            val r = new RestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId(), applicationContext);
            val request = new MockHttpServletRequest();
            val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
            val res = r.shouldMultifactorAuthenticationProviderExecute(MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
                    registeredService, provider, request, MultifactorAuthenticationTestUtils.getService("service"));

            val recordedRequest = webServer.takeRequest();
            val recordedRequestUrl = recordedRequest.getRequestUrl();
            assertEquals("casuser", recordedRequestUrl.queryParameter("principal"));
            assertEquals(recordedRequestUrl.queryParameter("service"), registeredService.getServiceId());
            assertEquals(recordedRequestUrl.queryParameter("provider"), provider.getId());
            assertEquals("HeaderValue", recordedRequest.getHeader("X-Custom-Header"));
            assertEquals("Bearer token", recordedRequest.getHeader("Authorization"));
            assertTrue(res);
        }
    }
}
