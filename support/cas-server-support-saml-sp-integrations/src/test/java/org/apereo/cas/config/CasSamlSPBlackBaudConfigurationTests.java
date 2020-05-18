package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPBlackBaudConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPBlackBaudConfiguration.class)
public class CasSamlSPBlackBaudConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.black-baud.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.black-baud.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.black-baud.name-id-format", () -> "transient");
    }
}
