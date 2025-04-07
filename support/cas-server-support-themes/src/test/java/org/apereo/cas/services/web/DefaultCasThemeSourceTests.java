package org.apereo.cas.services.web;

import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.themes.ThemeProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.NoSuchMessageException;
import org.springframework.test.context.TestPropertySource;
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
@ExtendWith(CasTestExtension.class)
class DefaultCasThemeSourceTests {

    @SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier("themeSource")
        protected ThemeSource themeSource;
    }

    @Nested
    @TestPropertySource(properties = "cas.view.template-prefixes[0]=classpath:/ext-templates")
    class CustomPrefixes extends BaseTests {
        @Test
        void verifyCustomSource() {
            val theme = themeSource.getTheme("my-theme");
            assertNotNull(theme);
            val message = theme.getMessageSource().getMessage("cas.theme.name",
                ArrayUtils.EMPTY_OBJECT_ARRAY, Locale.getDefault());
            assertEquals("MyTheme", message);
        }
    }

    @Nested
    class CustomTheme extends BaseTests {
        @Test
        void verifyCustomSource() {
            val theme = themeSource.getTheme("custom-theme");
            assertNotNull(theme);
            var message = theme.getMessageSource().getMessage("theme.custom.property",
                ArrayUtils.EMPTY_OBJECT_ARRAY, Locale.getDefault());
            assertEquals("CAS", message);
            message = theme.getMessageSource().getMessage("theme.default.property",
                ArrayUtils.EMPTY_OBJECT_ARRAY, Locale.getDefault());
            assertEquals("Apereo", message);
        }
    }

    @Nested
    class DefaultTheme extends BaseTests {
        @Test
        void verifyCustomSource() {
            val theme = themeSource.getTheme(ThemeProperties.DEFAULT_THEME_NAME);
            assertNotNull(theme);
            val message = theme.getMessageSource().getMessage("theme.default.property",
                ArrayUtils.EMPTY_OBJECT_ARRAY, Locale.getDefault());
            assertEquals("Apereo", message);
            assertThrows(NoSuchMessageException.class, () -> theme.getMessageSource().getMessage("theme.custom.property",
                ArrayUtils.EMPTY_OBJECT_ARRAY, Locale.getDefault()));
        }
    }
}
