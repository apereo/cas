package org.apereo.cas.jmx;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasJmxAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import javax.management.MBeanServerFactory;

/**
 * This is {@link BaseCasJmxTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseCasJmxTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasJmxAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
        @Bean
        public static BeanPostProcessor jmxTestMBeanServerConfigurer() {
            return new BeanPostProcessor() {
                @Override
                public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
                    if (bean instanceof final AnnotationMBeanExporter exporter) {
                        exporter.setServer(MBeanServerFactory.newMBeanServer());
                    }
                    return bean;
                }
            };
        }
    }
}
