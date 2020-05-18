package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPCraniumCafeConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPCraniumCafeConfiguration.class)
public class CasSamlSPCraniumCafeConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.cranium-cafe.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.cranium-cafe.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.cranium-cafe.name-id-format", () -> "transient");
    }

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getCraniumCafe().getEntityIds().get(0);
    }
}
