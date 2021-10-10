package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.thymeleaf.IEngineConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestfulUrlTemplateResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
public class RestfulUrlTemplateResolverTests {
    @Test
    public void verifyAction() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        try (val webServer = new MockWebServer(9302,
            new ByteArrayResource("template".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getView().getRest().setUrl("http://localhost:9302");
            var themeResolver = new FixedThemeResolver();
            themeResolver.setDefaultThemeName("sample-theme");
            val r = new RestfulUrlTemplateResolver(props, themeResolver);
            val res = r.resolveTemplate(mock(IEngineConfiguration.class), "cas",
                "template", new LinkedHashMap<>());
            assertNotNull(res);
        }
    }

    @Test
    public void verifyUnknownErrorAction() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        try (val webServer = new MockWebServer(9302,
            new ByteArrayResource("template".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.NO_CONTENT)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getView().getRest().setUrl("http://localhost:9302");
            val r = new RestfulUrlTemplateResolver(props, new FixedThemeResolver());
            val res = r.resolveTemplate(mock(IEngineConfiguration.class), "cas",
                "template", new LinkedHashMap<>());
            assertNotNull(res);
        }

    }
}
