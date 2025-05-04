package org.apereo.cas.multitenancy;

import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTenantsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Web")
@SpringBootTest(classes = BaseMultitenancyTests.SharedTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json"
})
@ExtendWith(CasTestExtension.class)
class DefaultTenantsManagerTests {

    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.INITIALIZATION_VECTOR.getPropertyName(), "true");
    }

    @Autowired
    @Qualifier(CipherExecutor.BEAN_NAME_CAS_CONFIGURATION_CIPHER_EXECUTOR)
    private CipherExecutor<String, String> casConfigurationCipherExecutor;

    @Autowired
    @Qualifier(TenantsManager.BEAN_NAME)
    private TenantsManager tenantsManager;

    @Test
    void verifyOperation() {
        val definition = tenantsManager.findTenant("b9584c42");
        assertTrue(definition.isPresent());
        val hostedDefinition = tenantsManager.findTenant("hosted").orElseThrow();
        assertEquals("sso.system.org", hostedDefinition.getProperties().get("cas.host.name"));
    }
}
