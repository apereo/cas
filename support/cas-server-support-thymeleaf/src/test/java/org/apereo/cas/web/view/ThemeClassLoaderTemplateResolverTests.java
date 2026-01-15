package org.apereo.cas.web.view;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.theme.FixedThemeResolver;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.thymeleaf.IEngineConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ThemeClassLoaderTemplateResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = ThymeleafAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class ThemeClassLoaderTemplateResolverTests {
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
    void verifyOperationByDefaultValue() {
        verifyThemeFile("test");
    }
}
