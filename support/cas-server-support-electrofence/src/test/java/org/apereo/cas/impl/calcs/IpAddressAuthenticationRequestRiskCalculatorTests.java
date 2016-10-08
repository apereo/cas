package org.apereo.cas.impl.calcs;

import org.apereo.cas.config.ElectroFenceConfiguration;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link IpAddressAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, 
        ElectroFenceConfiguration.class, CasCoreEventsConfiguration.class})
@TestPropertySource(locations = {"classpath:/ip-calc.properties"})
public class IpAddressAuthenticationRequestRiskCalculatorTests {
    
    @Test
    public void verifyTest() {}
}
