package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPCrashPlanConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.crash-plan.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.crash-plan.name-id-attribute=cn",
    "cas.saml-sp.crash-plan.name-id-format=transient"
})
public class CasSamlSPCrashPlanConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
