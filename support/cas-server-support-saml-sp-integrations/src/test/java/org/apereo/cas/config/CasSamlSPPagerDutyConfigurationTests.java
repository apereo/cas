package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This is {@link CasSamlSPPagerDutyConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPPagerDutyConfiguration.class)
public class CasSamlSPPagerDutyConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @DynamicPropertySource
    @SuppressWarnings("UnusedMethod")
    public static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.saml-sp.pager-duty.metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.saml-sp.pager-duty.name-id-attribute", () -> "cn");
        registry.add("cas.saml-sp.pager-duty.name-id-format", () -> "transient");
    }
}
