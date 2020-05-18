package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPOffice365ConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPOffice365Configuration.class)
public class CasSamlSPOffice365ConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.office365.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.office365.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.office365.name-id-format", () -> "transient");
    }
}
