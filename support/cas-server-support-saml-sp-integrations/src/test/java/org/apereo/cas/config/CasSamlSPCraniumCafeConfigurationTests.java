package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPCraniumCafeConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.cranium-cafe.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.cranium-cafe.name-id-attribute=cn",
    "cas.saml-sp.cranium-cafe.name-id-format=transient"
})
public class CasSamlSPCraniumCafeConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getCraniumCafe().getEntityIds().get(0);
    }
}
