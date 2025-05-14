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
 * This is {@link DefaultIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAuthenticationTests.SharedTestConfiguration.class)
class DefaultIPAddressIntelligenceServiceTests {
    @Autowired
    @Qualifier(IPAddressIntelligenceService.BEAN_NAME)
    private IPAddressIntelligenceService ipAddressIntelligenceService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertTrue(ipAddressIntelligenceService.examine(context, "1.2.3.4").isAllowed());
    }

}
