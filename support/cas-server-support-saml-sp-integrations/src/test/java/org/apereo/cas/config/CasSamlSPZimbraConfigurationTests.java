package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPZimbraConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.zimbra.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.zimbra.name-id-attribute=cn",
    "cas.saml-sp.zimbra.name-id-format=transient"
})
public class CasSamlSPZimbraConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
