package org.apereo.cas.logging.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.web.ThreadContextMDCServletFilter;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.HashMap;

/**
 * This is {@link CasLoggingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casLoggingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasLoggingConfiguration {

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CookieRetrievingCookieGenerator> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @ConditionalOnBean(value = TicketRegistry.class)
    @Bean
    public FilterRegistrationBean threadContextMDCServletFilter() {
        val initParams = new HashMap<String, String>();
        val bean = new FilterRegistrationBean();
        bean.setFilter(new ThreadContextMDCServletFilter(ticketRegistrySupport.getIfAvailable(), this.ticketGrantingTicketCookieGenerator.getIfAvailable()));
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setInitParameters(initParams);
        bean.setName("threadContextMDCServletFilter");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }
}
