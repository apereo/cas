package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPNeoGovConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPNeoGovConfiguration.class)
public class CasSamlSPNeoGovConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.neo-gov.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.neo-gov.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.neo-gov.name-id-format", () -> "transient");
    }

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getNeoGov().getEntityIds().get(0);
    }
}
