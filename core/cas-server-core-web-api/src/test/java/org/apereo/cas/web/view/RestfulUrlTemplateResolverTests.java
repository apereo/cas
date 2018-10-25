package org.apereo.cas.web.view;

import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
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
@Category(RestfulApiCategory.class)
public class RestfulUrlTemplateResolverTests {
    @Test
    public void verifyAction() {
        try (val webServer = new MockWebServer(9302,
            new ByteArrayResource("template".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getView().getRest().setUrl("http://localhost:9302");
            val r = new RestfulUrlTemplateResolver(props);
            val res = r.resolveTemplate(mock(IEngineConfiguration.class), "cas",
                "template", new LinkedHashMap<>());
            assertNotNull(res);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }

    }
}
