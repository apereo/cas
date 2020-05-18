package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPBenefitFocusConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPBenefitFocusConfiguration.class)
public class CasSamlSPBenefitFocusConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.benefit-focus.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.benefit-focus.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.benefit-focus.name-id-format", () -> "transient");
    }
}
