package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.val;
import org.apache.catalina.filters.CsrfPreventionFilter;
import org.apache.catalina.filters.RemoteAddrFilter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;

/**
 * This is {@link CasEmbeddedContainerTomcatFiltersConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "CasEmbeddedContainerTomcatFiltersConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportAutoConfiguration(CasEmbeddedContainerTomcatConfiguration.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.ApacheTomcat)
public class CasEmbeddedContainerTomcatFiltersConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "tomcatCsrfPreventionFilter")
    public FilterRegistrationBean<CsrfPreventionFilter> tomcatCsrfPreventionFilter(
        final CasConfigurationProperties casProperties) {
        val bean = new FilterRegistrationBean();
        bean.setFilter(new CsrfPreventionFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("tomcatCsrfPreventionFilter");
        bean.setEnabled(casProperties.getServer().getTomcat().getCsrf().isEnabled());
        return bean;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "tomcatRemoteAddressFilter")
    public FilterRegistrationBean<RemoteAddrFilter> tomcatRemoteAddressFilter(final CasConfigurationProperties casProperties) {
        val bean = new FilterRegistrationBean();
        val addr = casProperties.getServer().getTomcat().getRemoteAddr();
        val filter = new RemoteAddrFilter();
        filter.setAllow(addr.getAllowedClientIpAddressRegex());
        filter.setDeny(addr.getDeniedClientIpAddressRegex());
        filter.setDenyStatus(HttpStatus.UNAUTHORIZED.value());
        bean.setFilter(filter);
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("tomcatRemoteAddressFilter");
        bean.setEnabled(casProperties.getServer().getTomcat().getRemoteAddr().isEnabled());
        return bean;
    }
}
