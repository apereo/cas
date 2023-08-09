package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.palantir.controller.DashboardController;
import org.apereo.cas.palantir.controller.SchemaController;
import org.apereo.cas.palantir.controller.ServicesController;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.Nonnull;

/**
 * This is {@link CasPalantirConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Palantir)
public class CasPalantirConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "palantirDashboardController")
    public DashboardController palantirDashboardController() {
        return new DashboardController();
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirSchemaController")
    public SchemaController palantirSchemaController() {
        return new SchemaController();
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirServicesController")
    public ServicesController palantirServicesController(@Qualifier(ServicesManager.BEAN_NAME)
                                                         final ObjectProvider<ServicesManager> servicesManager,
                                                         final ConfigurableApplicationContext applicationContext) {
        return new ServicesController(servicesManager, new RegisteredServiceJsonSerializer(applicationContext));
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
                        .requestMatchers(new AntPathRequestMatcher("/assets/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher(PalantirConstants.URL_PATH_PALANTIR + "/**")).authenticated()
                    )
                    .formLogin(customizer -> customizer.loginPage(CasWebSecurityConfigurer.ENDPOINT_URL_ADMIN_FORM_LOGIN)
                        .permitAll().successHandler(successHandler));
                return this;
            }
        };
    }
}
