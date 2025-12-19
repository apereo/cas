package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPInfiniteCampusConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.infinite-campus.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.infinite-campus.name-id-attribute=cn",
    "cas.saml-sp.infinite-campus.name-id-format=transient"
})
class CasSamlSPInfiniteCampusConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
