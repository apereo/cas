package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.palantir.controller.DashboardController;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.Nonnull;

/**
 * This is {@link CasPalantirWebMvcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Palantir)
@Configuration(value = "CasPalantirWebMvcConfiguration", proxyBeanMethods = false)
class CasPalantirWebMvcConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "palantirDashboardController")
    public DashboardController palantirDashboardController(
        final ConfigurableApplicationContext applicationContext,
        final EndpointLinksResolver endpointLinksResolver,
        final WebEndpointProperties webEndpointProperties,
        final CasConfigurationProperties casProperties) {
        return new DashboardController(casProperties, endpointLinksResolver, webEndpointProperties, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirWebMvcConfigurer")
    public WebMvcConfigurer palantirWebMvcConfigurer(final CasConfigurationProperties casProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(@Nonnull final ViewControllerRegistry registry) {
                registry.addViewController(CasWebSecurityConfigurer.ENDPOINT_URL_ADMIN_FORM_LOGIN)
                    .setViewName(CasWebflowConstants.VIEW_ID_ENDPOINT_ADMIN_LOGIN_VIEW);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirEndpointWebSecurityConfigurer")
    public CasWebSecurityConfigurer<HttpSecurity> palantirEndpointWebSecurityConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public CasWebSecurityConfigurer<HttpSecurity> finish(final HttpSecurity http) throws Exception {
                val successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
                successHandler.setTargetUrlParameter("redirectTo");
                successHandler.setDefaultTargetUrl(PalantirConstants.URL_PATH_PALANTIR);
                http.authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(PalantirConstants.URL_PATH_PALANTIR + "/**")).authenticated()
                    )
                    .formLogin(customizer -> customizer.loginPage(CasWebSecurityConfigurer.ENDPOINT_URL_ADMIN_FORM_LOGIN)
                        .permitAll().successHandler(successHandler));
                return this;
            }
        };
    }
}
