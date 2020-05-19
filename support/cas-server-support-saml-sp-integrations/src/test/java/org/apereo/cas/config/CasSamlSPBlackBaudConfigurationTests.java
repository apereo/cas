package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPBlackBaudConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.black-baud.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.black-baud.name-id-attribute=cn",
    "cas.saml-sp.black-baud.name-id-format=transient"
})
public class CasSamlSPBlackBaudConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
