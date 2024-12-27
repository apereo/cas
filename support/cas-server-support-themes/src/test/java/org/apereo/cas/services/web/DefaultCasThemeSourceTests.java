package org.apereo.cas.services.web;

import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.context.ThemeSource;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasThemeSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Web")
@SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class, properties = "cas.view.template-prefixes[0]=classpath:/ext-templates")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultCasThemeSourceTests {
    @Autowired
    @Qualifier("themeSource")
    private ThemeSource themeSource;

    @Test
    void verifyCustomSource() {
        val theme = themeSource.getTheme("my-theme");
        assertNotNull(theme);
        val message = theme.getMessageSource().getMessage("cas.theme.name",
            ArrayUtils.EMPTY_OBJECT_ARRAY, Locale.getDefault());
        assertEquals("MyTheme", message);
    }
}
