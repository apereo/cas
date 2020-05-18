package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPYujaConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPYujaConfiguration.class)
public class CasSamlSPYujaConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.yuja.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.yuja.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.yuja.name-id-format", () -> "transient");
    }
}
