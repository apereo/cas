package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPTableauConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPTableauConfiguration.class)
public class CasSamlSPTableauConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.tableau.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.tableau.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.tableau.name-id-format", () -> "transient");
    }

}
