package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPCraniumCafeConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.cranium-cafe.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.cranium-cafe.name-id-attribute=cn",
    "cas.saml-sp.cranium-cafe.name-id-format=transient"
})
class CasSamlSPCraniumCafeConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getCraniumCafe().getEntityIds().getFirst();
    }
}
