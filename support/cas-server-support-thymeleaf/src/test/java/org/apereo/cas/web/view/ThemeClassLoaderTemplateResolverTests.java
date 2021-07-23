package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.thymeleaf.IEngineConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ThemeClassLoaderTemplateResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    ThymeleafAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Web")
public class ThemeClassLoaderTemplateResolverTests {
    private static void verifyThemeFile(final String themeName) {
        val themeResolver = new FixedThemeResolver();
        themeResolver.setDefaultThemeName(themeName);
        val resolver = new ThemeClassLoaderTemplateResolver(themeResolver);
        resolver.setSuffix(".html");
        resolver.setCheckExistence(true);
        resolver.setPrefix("templates/%s/");
        val view = resolver.resolveTemplate(mock(IEngineConfiguration.class), StringUtils.EMPTY, "casLoginView", Map.of());
        assertNotNull(view);
    }

    @Test
    public void verifyOperationByDefaultValue() {
        verifyThemeFile("test");
    }
}
