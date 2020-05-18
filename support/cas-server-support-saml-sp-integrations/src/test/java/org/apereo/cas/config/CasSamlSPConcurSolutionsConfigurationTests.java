package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPConcurSolutionsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPConcurSolutionsConfiguration.class)
public class CasSamlSPConcurSolutionsConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.concur-solutions.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.concur-solutions.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.concur-solutions.name-id-format", () -> "transient");
    }
}
