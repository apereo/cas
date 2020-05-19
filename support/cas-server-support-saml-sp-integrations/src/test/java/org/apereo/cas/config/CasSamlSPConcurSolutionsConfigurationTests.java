package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPConcurSolutionsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.concur-solutions.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.concur-solutions.name-id-attribute=cn",
    "cas.saml-sp.concur-solutions.name-id-format=transient"
})
public class CasSamlSPConcurSolutionsConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
