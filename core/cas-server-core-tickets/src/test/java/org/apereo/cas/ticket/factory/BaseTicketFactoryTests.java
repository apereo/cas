package org.apereo.cas.ticket.factory;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link BaseTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseTicketFactoryTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseTicketFactoryTests {

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    protected TicketFactory ticketFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    protected TicketSerializationManager ticketSerializationManager;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    protected TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_PROXY_GRANTING_TICKET_TRACKING)
    protected TicketTrackingPolicy proxyGrantingTicketTrackingPolicy;

    @ImportAutoConfiguration({
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @SpringBootTestAutoConfigurations
    public static class SharedTestConfiguration {
    }
}
