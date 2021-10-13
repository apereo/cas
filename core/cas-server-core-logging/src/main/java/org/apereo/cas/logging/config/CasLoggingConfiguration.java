package org.apereo.cas.logging.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.web.LoggingConfigurationEndpoint;
import org.apereo.cas.logging.web.ThreadContextMDCServletFilter;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.util.HashMap;

/**
 * This is {@link CasLoggingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casLoggingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasLoggingConfiguration {

    @ConditionalOnBean(value = TicketRegistry.class)
    @ConditionalOnProperty(prefix = "cas.logging", name = "mdc-enabled", havingValue = "true", matchIfMissing = true)
    public static class CasMdcLoggingConfiguration {
        @Autowired
        @Bean
        public FilterRegistrationBean<ThreadContextMDCServletFilter> threadContextMDCServletFilter(
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator) {
            val filter = new ThreadContextMDCServletFilter(ticketRegistrySupport, ticketGrantingTicketCookieGenerator);
            val initParams = new HashMap<String, String>();
            val bean = new FilterRegistrationBean<ThreadContextMDCServletFilter>();
            bean.setFilter(filter);
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setInitParameters(initParams);
            bean.setName("threadContextMDCServletFilter");
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
            return bean;
        }
    }

    /**
     * Log4j configuration.
     */
    @ConditionalOnClass(value = LoggerContext.class)
    @Configuration(value = "casLog4jConfiguration", proxyBeanMethods = false)
    public static class CasLog4jConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public LoggingConfigurationEndpoint loggingConfigurationEndpoint(final CasConfigurationProperties casProperties,
                                                                         final Environment environment,
                                                                         final ResourceLoader resourceLoader) {
            return new LoggingConfigurationEndpoint(casProperties, resourceLoader, environment);
        }

        @Bean
        public ServletListenerRegistrationBean log4jServletContextListener() {
            val bean = new ServletListenerRegistrationBean<Log4jServletContextListener>();
            bean.setEnabled(true);
            bean.setListener(new Log4jServletContextListener());
            return bean;
        }
    }

}
