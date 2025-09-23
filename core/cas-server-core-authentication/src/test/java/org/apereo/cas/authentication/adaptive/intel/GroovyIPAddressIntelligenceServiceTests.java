package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.authentication.BaseAuthenticationTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("GroovyMfa")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(
    classes = BaseAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.adaptive.ip-intel.groovy.location=classpath:GroovyIPAddressIntelligenceService.groovy")
class GroovyIPAddressIntelligenceServiceTests {
    @Autowired
    @Qualifier(IPAddressIntelligenceService.BEAN_NAME)
    private IPAddressIntelligenceService ipAddressIntelligenceService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Throwable {
        val requestContext = MockRequestContext.create(applicationContext);
        val response = ipAddressIntelligenceService.examine(requestContext, "1.2.3.4");
        assertTrue(response.isBanned());
    }
}
