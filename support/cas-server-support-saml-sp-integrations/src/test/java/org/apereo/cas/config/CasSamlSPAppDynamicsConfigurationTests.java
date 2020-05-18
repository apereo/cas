package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPAppDynamicsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPAppDynamicsConfiguration.class)
public class CasSamlSPAppDynamicsConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.app-dynamics.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.app-dynamics.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.app-dynamics.name-id-format", () -> "transient");
    }
}
