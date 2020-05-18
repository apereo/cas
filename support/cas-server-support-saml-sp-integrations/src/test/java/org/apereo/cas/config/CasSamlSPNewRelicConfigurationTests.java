package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPNewRelicConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPNewRelicConfiguration.class)
public class CasSamlSPNewRelicConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.new-relic.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.new-relic.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.new-relic.name-id-format", () -> "transient");
    }
}
