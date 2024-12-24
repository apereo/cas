package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.JsonTenantsManager;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.multitenancy.web.flow.CasMultitenancyFlowIdExtractor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasFlowIdExtractor;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * This is {@link CasMultitenancyAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
@AutoConfiguration
public class CasMultitenancyAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = TenantsManager.BEAN_NAME)
    public TenantsManager tenantsManager(final CasConfigurationProperties casProperties) throws Exception {
        return new JsonTenantsManager(casProperties.getMultitenancy().getJson().getLocation());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casMultitenancyFlowIdExtractor")
    public CasFlowIdExtractor casMultitenancyFlowIdExtractor(
        @Qualifier(TenantsManager.BEAN_NAME) final TenantsManager tenantsManager) {
        return new CasMultitenancyFlowIdExtractor(tenantsManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casMultitenancyEndpointConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebSecurityConfigurer<HttpSecurity> casMultitenancyEndpointConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            @CanIgnoreReturnValue
            public CasWebSecurityConfigurer<HttpSecurity> configure(final HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(customizer -> {
                    val authEndpoints = new AntPathRequestMatcher("/tenants/**");
                    customizer.requestMatchers(authEndpoints).permitAll();
                });
                return this;
            }
        };
    }
}
