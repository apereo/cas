package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPArmsSoftwareConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPArmsSoftwareConfiguration.class)
public class CasSamlSPArmsSoftwareConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.arms-software.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.arms-software.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.arms-software.name-id-format", () -> "transient");
    }
    
    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getArmsSoftware().getEntityIds().get(0);
    }
}
