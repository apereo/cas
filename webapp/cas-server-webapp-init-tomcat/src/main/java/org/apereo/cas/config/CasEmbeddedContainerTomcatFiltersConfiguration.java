package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.apache.catalina.filters.CsrfPreventionFilter;
import org.apache.catalina.filters.RemoteAddrFilter;
import org.apache.catalina.filters.RequestFilter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * This is {@link CasEmbeddedContainerTomcatFiltersConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportAutoConfiguration(CasEmbeddedContainerTomcatConfiguration.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ApacheTomcat)
@AutoConfiguration
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
        val filter = new ClientInfoRemoteAddrFilter();
        filter.setAllow(addr.getAllowedClientIpAddressRegex());
        filter.setDeny(addr.getDeniedClientIpAddressRegex());
        filter.setDenyStatus(HttpStatus.UNAUTHORIZED.value());
        bean.setFilter(filter);
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("clientInfoRemoteAddrFilter");
        bean.setEnabled(addr.isEnabled());
        return bean;
    }

    @Getter(AccessLevel.PROTECTED)
    private static class ClientInfoRemoteAddrFilter extends RequestFilter {
        //CHECKSTYLE:OFF
        private final Log logger = LogFactory.getLog(ClientInfoRemoteAddrFilter.class);
        //CHECKSTYLE:ON

        @Override
        public void doFilter(final ServletRequest request,
                             final ServletResponse response,
                             final FilterChain filterChain) throws ServletException, IOException {
            val remoteAddress = Optional.ofNullable(ClientInfoHolder.getClientInfo())
                .map(ClientInfo::getClientIpAddress)
                .orElseGet(request::getRemoteAddr);
            val message = String.format("Remote address to process is [%s]", remoteAddress);
            logger.trace(message);
            process(remoteAddress, request, response, filterChain);
        }
    }
}
