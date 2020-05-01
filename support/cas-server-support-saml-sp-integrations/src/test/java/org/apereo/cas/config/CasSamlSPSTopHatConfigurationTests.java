package org.apereo.cas.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlSPSTopHatConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPSTopHatConfiguration.class)
public class CasSamlSPSTopHatConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @BeforeAll
    public static void beforeAll() {
        SERVICE_PROVIDER = "topHat";
    }
}
