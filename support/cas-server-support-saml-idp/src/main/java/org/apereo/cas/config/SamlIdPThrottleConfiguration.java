package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.Nonnull;

/**
 * This is {@link SamlIdPThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnBean(name = AuthenticationThrottlingExecutionPlan.BEAN_NAME)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.Throttling,
    CasFeatureModule.FeatureCatalog.SAMLIdentityProvider
})
@Configuration(value = "SamlIdPThrottleConfiguration", proxyBeanMethods = false)
class SamlIdPThrottleConfiguration {

    @Configuration(value = "SamlIdPThrottleWebMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPThrottleWebMvcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPThrottleWebMvcConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebMvcConfigurer samlIdPThrottleWebMvcConfigurer(
            @Qualifier(AuthenticationThrottlingExecutionPlan.BEAN_NAME)
            final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(@Nonnull final InterceptorRegistry registry) {
                    val handler = new RefreshableHandlerInterceptor(
                        () -> authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors());
                    registry.addInterceptor(handler)
                        .order(0)
                        .addPathPatterns('/' + SamlIdPConstants.BASE_ENDPOINT_IDP + "/**");
                }
            };
        }
    }
}
