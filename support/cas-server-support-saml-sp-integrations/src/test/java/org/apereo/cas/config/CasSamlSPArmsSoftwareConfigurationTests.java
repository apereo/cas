package org.apereo.cas.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlSPArmsSoftwareConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPArmsSoftwareConfiguration.class)
public class CasSamlSPArmsSoftwareConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @BeforeAll
    public static void beforeAll() {
        SERVICE_PROVIDER = "armsSoftware";
    }
    
    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getArmsSoftware().getEntityIds().get(0);
    }
}
