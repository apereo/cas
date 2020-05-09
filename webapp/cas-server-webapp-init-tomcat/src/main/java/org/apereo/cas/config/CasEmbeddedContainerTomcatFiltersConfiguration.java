package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.catalina.filters.CsrfPreventionFilter;
import org.apache.catalina.filters.RemoteAddrFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * This is {@link CasEmbeddedContainerTomcatFiltersConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casEmbeddedContainerTomcatFiltersConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportAutoConfiguration(CasEmbeddedContainerTomcatConfiguration.class)
public class CasEmbeddedContainerTomcatFiltersConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnProperty(prefix = "cas.server.tomcat.csrf", name = "enabled", havingValue = "true")
    @RefreshScope
    @Bean
    public FilterRegistrationBean tomcatCsrfPreventionFilter() {
        val bean = new FilterRegistrationBean();
        bean.setFilter(new CsrfPreventionFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("tomcatCsrfPreventionFilter");
        return bean;
    }

    @ConditionalOnProperty(prefix = "cas.server.tomcat.remote-addr", name = "enabled", havingValue = "true")
    @RefreshScope
    @Bean
    public FilterRegistrationBean tomcatRemoteAddressFilter() {
        val bean = new FilterRegistrationBean();
        val addr = casProperties.getServer().getTomcat().getRemoteAddr();
        val filter = new RemoteAddrFilter();
        filter.setAllow(addr.getAllowedClientIpAddressRegex());
        filter.setDeny(addr.getDeniedClientIpAddressRegex());
        filter.setDenyStatus(HttpStatus.UNAUTHORIZED.value());
        bean.setFilter(filter);
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("tomcatRemoteAddressFilter");
        return bean;
    }
}
