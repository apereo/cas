package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPEasyIepConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.easy-iep.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.easy-iep.name-id-attribute=cn",
    "cas.saml-sp.easy-iep.name-id-format=transient"
})
public class CasSamlSPEasyIepConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
