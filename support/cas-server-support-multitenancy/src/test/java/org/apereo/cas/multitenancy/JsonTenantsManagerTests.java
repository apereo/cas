package org.apereo.cas.multitenancy;

import org.apereo.cas.config.CasMultitenancyAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonTenantsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Web")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasMultitenancyAutoConfiguration.class,
    properties = "cas.multitenancy.json.location=classpath:/tenants.json")
class JsonTenantsManagerTests {

    @Autowired
    @Qualifier(TenantsManager.BEAN_NAME)
    private TenantsManager tenantsManager;

    @Test
    void verifyOperation() throws Exception {
        assertTrue(tenantsManager.findTenant("b9584c42").isPresent());
    }
}
