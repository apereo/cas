package org.apereo.cas.logging.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logging.web.LoggingConfigurationEndpoint;
import org.apereo.cas.logging.web.ThreadContextMDCServletFilter;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@AutoConfiguration
public class CasLoggingConfiguration {

    @ConditionalOnBean(TicketRegistry.class)
    public static class CasMdcLoggingConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean<ThreadContextMDCServletFilter> threadContextMDCServletFilter(
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistrySupport.BEAN_NAME) final ObjectProvider<TicketRegistrySupport> ticketRegistrySupport,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER) final ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator) {
            val filter = new ThreadContextMDCServletFilter(ticketRegistrySupport, ticketGrantingTicketCookieGenerator);
            val initParams = new HashMap<String, String>();
            val bean = new FilterRegistrationBean<ThreadContextMDCServletFilter>();
            bean.setFilter(filter);
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setInitParameters(initParams);
            bean.setName("threadContextMDCServletFilter");
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
            bean.setEnabled(casProperties.getLogging().isMdcEnabled());
            return bean;
        }
    }

    /**
     * Log4j configuration.
     */
    @ConditionalOnClass(LoggerContext.class)
    @Configuration(value = "CasLog4jConfiguration", proxyBeanMethods = false)
    public static class CasLog4jConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LoggingConfigurationEndpoint loggingConfigurationEndpoint(
            final CasConfigurationProperties casProperties,
            final Environment environment,
            final ResourceLoader resourceLoader) {
            return new LoggingConfigurationEndpoint(casProperties, resourceLoader, environment);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServletListenerRegistrationBean<Log4jServletContextListener> log4jServletContextListener() {
            val bean = new ServletListenerRegistrationBean<Log4jServletContextListener>();
            bean.setEnabled(true);
            bean.setListener(new Log4jServletContextListener());
            return bean;
        }
    }

}
