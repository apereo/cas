package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPGiveCampusConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.give-campus.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.give-campus.name-id-attribute=cn",
    "cas.saml-sp.give-campus.name-id-format=transient"
})
class CasSamlSPGiveCampusConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
