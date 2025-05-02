package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.DefaultTenantExtractor;
import org.apereo.cas.multitenancy.DefaultTenantsManager;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantWebflowDecorator;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * This is {@link CasCoreMultitenancyAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
@AutoConfiguration
public class CasCoreMultitenancyAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.multitenancy.core.enabled").isTrue();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = TenantsManager.BEAN_NAME)
    public TenantsManager tenantsManager(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(TenantsManager.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val location = casProperties.getMultitenancy().getJson().getLocation();
                return new DefaultTenantsManager(location);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = TenantExtractor.BEAN_NAME)
    public TenantExtractor tenantExtractor(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(TenantsManager.BEAN_NAME)
        final TenantsManager tenantsManager) {
        return new DefaultTenantExtractor(tenantsManager, applicationContext, casProperties);
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
                    val authEndpoints = PathPatternRequestMatcher.withDefaults().matcher("/tenants/**");
                    customizer.requestMatchers(authEndpoints).permitAll();
                });
                return this;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "casMultitenancyWebflowDecorator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public WebflowDecorator casMultitenancyWebflowDecorator(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(WebflowDecorator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new TenantWebflowDecorator(tenantExtractor))
            .otherwise(WebflowDecorator::noOp)
            .get();
    }
}
