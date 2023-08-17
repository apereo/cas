package org.apereo.cas.support.saml;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.ConfigurationService;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenSamlConfigurationPropertiesSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAML")
public class OpenSamlConfigurationPropertiesSourceTests {
    @Test
    void verifyOperation() {
        val source = ConfigurationService.get(OpenSamlConfigurationPropertiesSource.class);
        assertFalse(Objects.requireNonNull(source.getProperties()).isEmpty());
    }
}
