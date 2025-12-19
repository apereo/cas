package org.apereo.cas.support.saml;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.ConfigurationService;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenSamlConfigurationPropertiesSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAML")
class OpenSamlConfigurationPropertiesSourceTests {
    @Test
    void verifyOperation() {
        val source = ConfigurationService.getConfigurationProperties();
        assertFalse(source.getProperty(OpenSamlConfigurationPropertiesSource.CONFIG_APACHE_XML_IGNORE_LINEBREAKS).isEmpty());
        assertFalse(source.getProperty(OpenSamlConfigurationPropertiesSource.CONFIG_STRICT_MODE).isEmpty());
    }
}
