package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPSafariOnlineConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPSafariOnlineConfiguration.class)
public class CasSamlSPSafariOnlineConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.safari-online.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.safari-online.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.safari-online.name-id-format", () -> "transient");
    }

}
