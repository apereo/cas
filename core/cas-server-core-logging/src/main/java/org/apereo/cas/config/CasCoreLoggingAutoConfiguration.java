package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logging.web.LoggingConfigurationEndpoint;
import org.apereo.cas.logging.web.ThreadContextMDCServletFilter;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

/**
 * This is {@link CasCoreLoggingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@AutoConfiguration
public class CasCoreLoggingAutoConfiguration {

    @Configuration(value = "CasMdcLoggingConfiguration", proxyBeanMethods = false)
    static class CasMdcLoggingConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean<@NonNull ThreadContextMDCServletFilter> threadContextMDCServletFilter(
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final ObjectProvider<@NonNull TicketRegistrySupport> ticketRegistrySupport,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final ObjectProvider<@NonNull CasCookieBuilder> ticketGrantingTicketCookieGenerator) {
            val filter = new ThreadContextMDCServletFilter(ticketRegistrySupport,
                ticketGrantingTicketCookieGenerator, casProperties);
            val bean = new FilterRegistrationBean<@NonNull ThreadContextMDCServletFilter>();
            bean.setFilter(filter);
            bean.setAsyncSupported(true);
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("threadContextMDCServletFilter");
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
            bean.setEnabled(casProperties.getLogging().getMdc().isEnabled());
            return bean;
        }
    }

    /**
     * Log4j configuration.
     */
    @ConditionalOnClass(LoggerContext.class)
    @Configuration(value = "CasLog4jConfiguration", proxyBeanMethods = false)
    static class CasLog4jConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LoggingConfigurationEndpoint loggingConfigurationEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new LoggingConfigurationEndpoint(casProperties, applicationContext);
        }

        @Bean
        public ServletListenerRegistrationBean<@NonNull Log4jServletContextListener> log4jServletContextListener() {
            val bean = new ServletListenerRegistrationBean<@NonNull Log4jServletContextListener>();
            bean.setEnabled(true);
            bean.setListener(new Log4jServletContextListener());
            return bean;
        }
    }

}
