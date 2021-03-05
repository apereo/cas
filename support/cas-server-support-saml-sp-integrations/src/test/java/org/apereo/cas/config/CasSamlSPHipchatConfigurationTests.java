package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPHipchatConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.hipchat.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.hipchat.name-id-attribute=cn",
    "cas.saml-sp.hipchat.name-id-format=transient"
})
public class CasSamlSPHipchatConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
