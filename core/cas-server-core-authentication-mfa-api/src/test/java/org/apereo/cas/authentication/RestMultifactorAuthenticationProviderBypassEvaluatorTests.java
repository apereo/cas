package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.RestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import okhttp3.mockwebserver.MockResponse;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApiAuthentication")
public class RestMultifactorAuthenticationProviderBypassEvaluatorTests {
    @Test
    public void verifyOperationShouldProceed() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        try (val webServer = new MockWebServer(9316,
            new ByteArrayResource("Y".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.ACCEPTED)) {
            webServer.start();

            val props = new MultifactorAuthenticationProviderBypassProperties();
            props.getRest().setUrl("http://localhost:9316");
            val provider = new TestMultifactorAuthenticationProvider();
            val r = new RestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
            val res = r.shouldMultifactorAuthenticationProviderExecute(MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
                MultifactorAuthenticationTestUtils.getRegisteredService(), provider,
                new MockHttpServletRequest());
            assertTrue(res);
        }
    }

    @Test
    public void verifyOperationFailsWithNoProvider() {
        try (val webServer = new MockWebServer(9316,
            new ByteArrayResource("Y".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.ACCEPTED)) {
            webServer.start();

            val props = new MultifactorAuthenticationProviderBypassProperties();
            props.getRest().setUrl("http://localhost:9316");
            val provider = new TestMultifactorAuthenticationProvider();
            val r = new RestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
            val res = r.shouldMultifactorAuthenticationProviderExecute(MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
                MultifactorAuthenticationTestUtils.getRegisteredService(), null,
                new MockHttpServletRequest());
            assertTrue(res);
        }
    }

    @Test
    public void verifyRestSendsQueryParameters() throws Exception {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        try (val webServer = new okhttp3.mockwebserver.MockWebServer()) {
            val port = webServer.getPort();
            val response = new MockResponse().setResponseCode(HttpStatus.ACCEPTED.value());
            webServer.enqueue(response);
            webServer.start();

            val props = new MultifactorAuthenticationProviderBypassProperties();
            props.getRest().setUrl("http://localhost:" + port);
            val provider = new TestMultifactorAuthenticationProvider();
            val r = new RestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
            val request = new MockHttpServletRequest();
            val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
            val res = r.shouldMultifactorAuthenticationProviderExecute(MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
                registeredService, provider, request);

            val recordedRequestUrl = webServer.takeRequest().getRequestUrl();
            assertEquals(recordedRequestUrl.queryParameter("principal"), "casuser");
            assertEquals(recordedRequestUrl.queryParameter("service"), registeredService.getServiceId());
            assertEquals(recordedRequestUrl.queryParameter("provider"), provider.getId());
            assertTrue(res);
        }
    }
}
