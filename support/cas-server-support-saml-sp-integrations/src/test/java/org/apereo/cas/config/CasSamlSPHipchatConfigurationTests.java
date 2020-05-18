package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPHipchatConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPHipchatConfiguration.class)
public class CasSamlSPHipchatConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.hipchat.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.hipchat.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.hipchat.name-id-format", () -> "transient");
    }
}
