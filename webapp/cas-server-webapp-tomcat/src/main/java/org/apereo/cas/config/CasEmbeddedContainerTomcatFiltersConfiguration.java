package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.filters.CsrfPreventionFilter;
import org.apache.catalina.filters.RemoteAddrFilter;
import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatRemoteAddressProperties;
import org.apereo.cas.util.CollectionUtils;
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
@Configuration("casEmbeddedContainerTomcatFiltersConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = CasEmbeddedContainerUtils.EMBEDDED_CONTAINER_CONFIG_ACTIVE, havingValue = "true")
@ImportAutoConfiguration(CasEmbeddedContainerTomcatConfiguration.class)
@Slf4j
public class CasEmbeddedContainerTomcatFiltersConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnProperty(prefix = "cas.server.csrf", name = "enabled", havingValue = "true")
    @RefreshScope
    @Bean
    public FilterRegistrationBean tomcatCsrfPreventionFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new CsrfPreventionFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("tomcatCsrfPreventionFilter");
        return bean;
    }

    @ConditionalOnProperty(prefix = "cas.server.remoteAddr", name = "enabled", havingValue = "true")
    @RefreshScope
    @Bean
    public FilterRegistrationBean tomcatRemoteAddressFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        final CasEmbeddedApacheTomcatRemoteAddressProperties addr = casProperties.getServer().getRemoteAddr();
        final RemoteAddrFilter filter = new RemoteAddrFilter();
        filter.setAllow(addr.getAllowedClientIpAddressRegex());
        filter.setDeny(addr.getDeniedClientIpAddressRegex());
        filter.setDenyStatus(HttpStatus.UNAUTHORIZED.value());
        bean.setFilter(filter);
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("tomcatRemoteAddressFilter");
        return bean;
    }
}
