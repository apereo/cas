package org.apereo.cas.impl.calcs;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.ElectronicFenceConfiguration;
import org.apereo.cas.impl.MockTicketGrantingTicketCreatedEventProducer;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.support.geo.config.GoogleMapsGeoCodingConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
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
        ElectronicFenceConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreServicesConfiguration.class,
        GoogleMapsGeoCodingConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreEventsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreEventsConfiguration.class})
@TestPropertySource(properties = "cas.authn.adaptive.risk.ip.enabled=true")
@DirtiesContext
@EnableScheduling
public class IpAddressAuthenticationRequestRiskCalculatorTests {

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Before
    public void prepTest() {
        MockTicketGrantingTicketCreatedEventProducer.createEvents(this.casEventRepository);
    }

    @Test
    public void verifyTest() {
    }
}
