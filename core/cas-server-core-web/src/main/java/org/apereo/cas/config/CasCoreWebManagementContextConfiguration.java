package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoExtractionOptions;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ConditionalOnManagementPort;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * This is {@link CasCoreWebManagementContextConfiguration}.
 * This class is only activated when CAS is deployed in a separate management context
 * triggered when the management port is set to a different port.
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ManagementContextConfiguration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnManagementPort(ManagementPortType.DIFFERENT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Import(CasCoreActuatorsConfiguration.class)
class CasCoreWebManagementContextConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "casClientInfoLoggingFilterManagementContext")
    public FilterRegistrationBean<@NonNull ClientInfoThreadLocalFilter> casClientInfoLoggingFilterManagementContext(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        final CasConfigurationProperties casProperties) {
        val bean = new FilterRegistrationBean<@NonNull ClientInfoThreadLocalFilter>();
        val audit = casProperties.getAudit().getEngine();
        val options = ClientInfoExtractionOptions.builder()
            .alternateLocalAddrHeaderName(audit.getAlternateClientAddrHeaderName())
            .alternateServerAddrHeaderName(audit.getAlternateServerAddrHeaderName())
            .useServerHostAddress(audit.isUseServerHostAddress())
            .httpRequestHeaders(audit.getHttpRequestHeaders())
            .build();
        bean.setFilter(new ClientInfoThreadLocalFilter(options, tenantExtractor));
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("CAS Client Info Logging Filter");
        bean.setAsyncSupported(true);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }

}
