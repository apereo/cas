package org.apereo.cas.multitenancy;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTenantExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Web")
@SpringBootTest(classes = BaseMultitenancyTests.SharedTestConfiguration.class,
    properties = "cas.multitenancy.json.location=classpath:/tenants.json")
@ExtendWith(CasTestExtension.class)
class DefaultTenantExtractorTests {

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.setContextPath("/cas/tenants/b9584c42/login");
        val definition = tenantExtractor.extract(requestContext);
        assertTrue(definition.isPresent());
    }

    @Test
    void verifyBlank() {
        val definition = tenantExtractor.extract(StringUtils.EMPTY);
        assertFalse(definition.isPresent());
    }
}
