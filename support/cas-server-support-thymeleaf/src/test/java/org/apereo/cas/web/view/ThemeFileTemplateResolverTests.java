package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.thymeleaf.IEngineConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ThemeFileTemplateResolverTests}.
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
public class ThemeFileTemplateResolverTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperationByDefaultValue() throws Exception {
        val themeDir = new File(FileUtils.getTempDirectory(), "test");
        if (!themeDir.exists() && !themeDir.mkdir()) {
            fail(() -> "Unable to create directory " + themeDir);
        }
        val path = new File(themeDir, "casLoginView.html");
        FileUtils.write(path, "<html><html>", StandardCharsets.UTF_8);

        val themeResolver = new FixedThemeResolver();
        themeResolver.setDefaultThemeName("test");
        val resolver = new ThemeFileTemplateResolver(casProperties, themeResolver);
        resolver.setSuffix(".html");
        resolver.setCheckExistence(true);
        resolver.setPrefix(FileUtils.getTempDirectoryPath() + "/%s/");
        val view = resolver.resolveTemplate(mock(IEngineConfiguration.class), StringUtils.EMPTY, "casLoginView", Map.of());
        assertNotNull(view);
    }
}
