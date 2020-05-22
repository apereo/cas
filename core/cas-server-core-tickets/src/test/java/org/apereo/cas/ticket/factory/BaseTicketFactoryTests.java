package org.apereo.cas.ticket.factory;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * This is {@link BaseTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    BaseTicketFactoryTests.TicketFactoryTestConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketComponentSerializationConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseTicketFactoryTests {

    @Autowired
    @Qualifier("defaultTicketFactory")
    protected TicketFactory ticketFactory;

    @TestConfiguration("TicketFactoryTestConfiguration")
    @Lazy(false)
    public static class TicketFactoryTestConfiguration {
        @Bean
        public List inMemoryRegisteredServices() {
            val svc = RegisteredServiceTestUtils.getRegisteredService("customExpirationPolicy", RegexRegisteredService.class);
            svc.setServiceTicketExpirationPolicy(
                new DefaultRegisteredServiceServiceTicketExpirationPolicy(10, "666"));
            svc.setProxyTicketExpirationPolicy(
                new DefaultRegisteredServiceProxyTicketExpirationPolicy(50, "1984"));

            val defaultSvc = RegisteredServiceTestUtils.getRegisteredService("defaultExpirationPolicy", RegexRegisteredService.class);
            return CollectionUtils.wrapList(svc, defaultSvc);
        }
    }
}
