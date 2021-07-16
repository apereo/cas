package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSymplicityConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.symplicity.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.symplicity.name-id-attribute=cn",
    "cas.saml-sp.symplicity.name-id-format=transient"
})
public class CasSamlSPSymplicityConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
