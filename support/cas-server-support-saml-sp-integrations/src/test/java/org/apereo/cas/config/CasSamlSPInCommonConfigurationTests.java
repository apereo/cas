package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPInCommonConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.in-common.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.in-common.name-id-attribute=cn",
    "cas.saml-sp.in-common.name-id-format=transient"
})
class CasSamlSPInCommonConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
