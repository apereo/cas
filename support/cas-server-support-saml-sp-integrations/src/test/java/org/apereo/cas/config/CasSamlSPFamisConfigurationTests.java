package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPFamisConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPFamisConfiguration.class)
public class CasSamlSPFamisConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.famis.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.famis.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.famis.name-id-format", () -> "transient");
    }
}
