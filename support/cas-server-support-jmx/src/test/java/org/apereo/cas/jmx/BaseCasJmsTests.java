package org.apereo.cas.jmx;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasJmxConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseCasJmsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseCasJmsTests {
    
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasJmxConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreUtilConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
