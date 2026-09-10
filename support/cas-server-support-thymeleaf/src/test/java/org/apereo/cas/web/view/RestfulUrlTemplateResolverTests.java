package org.apereo.cas.web.view;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.theme.FixedThemeResolver;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.IEngineConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestfulUrlTemplateResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
class RestfulUrlTemplateResolverTests {

    @BeforeEach
    void setup() {
        RequestContextHolder.resetRequestAttributes();
        setRequestAttributes();
    }

    @Test
    void verifyAction() {
        try (val webServer = new MockWebServer("template")) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getView().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val themeResolver = new FixedThemeResolver();
            themeResolver.setDefaultThemeName("sample-theme");
            val resolver = new RestfulUrlTemplateResolver(props, themeResolver);
            val res = resolver.resolveTemplate(mock(IEngineConfiguration.class), "cas",
                "template", new LinkedHashMap<>());
            assertNotNull(res);
        }
    }

    private static void setRequestAttributes() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.example.org/cas/login?key1=value1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyCredentialHeadersAreNotForwarded() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.example.org/cas/login");
        request.addHeader(HttpHeaders.COOKIE, "TGC=TGT-1-abcdef");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic Y2FzdXNlcjpNZWxsb24=");
        request.addHeader(HttpHeaders.PROXY_AUTHORIZATION, "Basic Y2FzdXNlcjpNZWxsb24=");
        request.addHeader("X-Custom-Header", "custom-value");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));

        val received = new ConcurrentHashMap<String, String>();
        try (val webServer = new MockWebServer("template")) {
            webServer.headersConsumer(received::putAll);
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getView().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val resolver = new RestfulUrlTemplateResolver(props, new FixedThemeResolver());
            assertNotNull(resolver.resolveTemplate(mock(IEngineConfiguration.class), "cas",
                "template", new LinkedHashMap<>()));
        }

        val forwarded = received.keySet()
            .stream()
            .map(name -> name.toLowerCase(Locale.ENGLISH))
            .collect(Collectors.toSet());
        assertFalse(forwarded.contains(HttpHeaders.COOKIE.toLowerCase(Locale.ENGLISH)));
        assertFalse(forwarded.contains(HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ENGLISH)));
        assertFalse(forwarded.contains(HttpHeaders.PROXY_AUTHORIZATION.toLowerCase(Locale.ENGLISH)));
        assertTrue(forwarded.contains("x-custom-header"));
        assertTrue(forwarded.contains("template"));
    }

    @Test
    void verifyUnknownErrorAction() {
        try (val webServer = new MockWebServer("template", HttpStatus.NO_CONTENT)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getView().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val resolver = new RestfulUrlTemplateResolver(props, new FixedThemeResolver());
            val res = resolver.resolveTemplate(mock(IEngineConfiguration.class), "cas",
                "template", new LinkedHashMap<>());
            assertNotNull(res);
        }

    }
}
