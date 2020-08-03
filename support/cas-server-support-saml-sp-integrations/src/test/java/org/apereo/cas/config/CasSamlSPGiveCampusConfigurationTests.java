package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPGiveCampusConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.give-campus.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.give-campus.name-id-attribute=cn",
    "cas.saml-sp.give-campus.name-id-format=transient"
})
public class CasSamlSPGiveCampusConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
