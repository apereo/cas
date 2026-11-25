package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.palantir.controller.DashboardController;
import org.apereo.cas.palantir.controller.PalantirOverviewController;
import org.apereo.cas.palantir.overview.PalantirApplicationChanges;
import org.apereo.cas.palantir.overview.PalantirOverview;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.RestActuatorControllerEndpoint;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointAccessResolver;
import org.springframework.boot.actuate.endpoint.EndpointsSupplier;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        final WebEndpointsSupplier webEndpointsSupplier,
        @Qualifier("restControllerEndpointDiscoverer")
        final EndpointsSupplier<RestActuatorControllerEndpoint> restEndpointsSupplier,
        final EndpointAccessResolver endpointAccessResolver,
        final CasConfigurationProperties casProperties) {
        val endpoints = new ArrayList<ExposableEndpoint<?>>();
        endpoints.addAll(webEndpointsSupplier.getEndpoints());
        endpoints.addAll(restEndpointsSupplier.getEndpoints());
        return new DashboardController(casProperties, endpointLinksResolver, webEndpointProperties,
            applicationContext, endpoints, endpointAccessResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirOverview")
    public PalantirOverview palantirOverview(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ObjectProvider<ServicesManager> servicesManager,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final ObjectProvider<TicketRegistry> ticketRegistry,
        final ObjectProvider<MeterRegistry> meterRegistry,
        @Qualifier("palantirApplicationChanges")
        final ObjectProvider<PalantirApplicationChanges> palantirApplicationChanges) {
        return new PalantirOverview(applicationContext, servicesManager, ticketRegistry,
            meterRegistry, palantirApplicationChanges);
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirOverviewController")
    public PalantirOverviewController palantirOverviewController(
        @Qualifier("palantirOverview") final PalantirOverview palantirOverview,
        final EndpointLinksResolver endpointLinksResolver,
        final WebEndpointProperties webEndpointProperties,
        final ObjectProvider<HealthEndpointProperties> healthEndpointProperties) {
        return new PalantirOverviewController(palantirOverview, endpointLinksResolver, webEndpointProperties, healthEndpointProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirWebMvcConfigurer")
    public WebMvcConfigurer palantirWebMvcConfigurer(final CasConfigurationProperties casProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(@NonNull final ViewControllerRegistry registry) {
                registry.addViewController(CasWebSecurityConfigurer.ENDPOINT_URL_ADMIN_FORM_LOGIN)
                    .setViewName(CasWebflowConstants.VIEW_ID_ENDPOINT_ADMIN_LOGIN_VIEW);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirEndpointWebSecurityConfigurer")
    public CasWebSecurityConfigurer<HttpSecurity> palantirEndpointWebSecurityConfigurer(
        final WebEndpointProperties webEndpointProperties,
        final ObjectProvider<PathMappedEndpoints> pathMappedEndpoints) {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getCsrfProtectedSessionEndpoints() {
                val endpoints = new ArrayList<String>();
                endpoints.add(PalantirConstants.URL_PATH_PALANTIR + "/**");
                val basePath = Strings.CI.removeEnd(StringUtils.defaultString(webEndpointProperties.getBasePath()), "/");
                if (StringUtils.isNotBlank(basePath)) {
                    endpoints.add(basePath + "/**");
                } else {
                    pathMappedEndpoints.ifAvailable(mapped -> mapped.getAllRootPaths()
                        .stream()
                        .map(path -> Strings.CI.removeEnd(Strings.CI.prependIfMissing(path, "/"), "/"))
                        .filter(StringUtils::isNotBlank)
                        .map(path -> path + "/**")
                        .forEach(endpoints::add));
                }
                return endpoints;
            }

            @Override
            public CasWebSecurityConfigurer<HttpSecurity> finish(final HttpSecurity http) {
                val successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
                successHandler.setDefaultTargetUrl(PalantirConstants.URL_PATH_PALANTIR);
                http.authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(PathPatternRequestMatcher.withDefaults()
                            .matcher(PalantirConstants.URL_PATH_PALANTIR + "/**")).authenticated()
                    )
                    .logout(logout -> logout
                        .logoutUrl(PalantirConstants.URL_PATH_PALANTIR + "/dashboard/logout")
                        .logoutSuccessUrl(PalantirConstants.URL_PATH_PALANTIR + "/dashboard")
                    )
                    .formLogin(customizer -> customizer
                        .loginPage(CasWebSecurityConfigurer.ENDPOINT_URL_ADMIN_FORM_LOGIN)
                        .permitAll()
                        .successHandler(successHandler));
                return this;
            }
        };
    }
}
