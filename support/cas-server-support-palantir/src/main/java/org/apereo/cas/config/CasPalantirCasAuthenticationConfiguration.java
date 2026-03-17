package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.client.authentication.AttributePrincipalImpl;
import org.apereo.cas.client.validation.AssertionImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.TicketValidator;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * This is {@link CasPalantirCasAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Palantir, module = "cas-authentication", enabledByDefault = false)
@Configuration(value = "CasPalantirCasAuthenticationConfiguration", proxyBeanMethods = false)
@Lazy(false)
class CasPalantirCasAuthenticationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "palantirCasServerWebSecurityConfigurer")
    public CasWebSecurityConfigurer<HttpSecurity> palantirCasServerWebSecurityConfigurer(
        @Qualifier("palantirServiceProperties")
        final ServiceProperties serviceProperties,
        final CasConfigurationProperties casProperties,
        @Qualifier("palantirCasAuthenticationProvider")
        final CasAuthenticationProvider casAuthenticationProvider,
        @Qualifier("palantirCasAuthenticationFilter")
        final CasAuthenticationFilter casAuthenticationFilter,
        @Qualifier("palantirCasAuthenticationEntryPoint")
        final CasAuthenticationEntryPoint casAuthenticationEntryPoint) {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public CasWebSecurityConfigurer<HttpSecurity> configure(final HttpSecurity http) {
                http.authorizeHttpRequests(customizer -> customizer
                    .requestMatchers(PathPatternRequestMatcher.withDefaults()
                        .matcher(PalantirConstants.URL_PATH_PALANTIR + "/**"))
                    .authenticated()
                );
                http.authenticationProvider(casAuthenticationProvider);
                http.exceptionHandling(ex -> ex.authenticationEntryPoint(casAuthenticationEntryPoint)).addFilter(casAuthenticationFilter);
                http.logout(customizer -> {
                    customizer.logoutUrl(PalantirConstants.URL_PATH_PALANTIR + "/dashboard/logout");
                    customizer.logoutSuccessHandler((request, response, authentication) -> {
                        val redirectUrl = casProperties.getServer().getLogoutUrl()
                            + '?' + serviceProperties.getServiceParameter() + '='
                            + casProperties.getServer().getPrefix() + PalantirConstants.URL_PATH_PALANTIR;
                        response.sendRedirect(redirectUrl);
                    });
                });
                return this;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirServiceProperties")
    public ServiceProperties palantirServiceProperties(final CasConfigurationProperties casProperties) {
        val sp = new ServiceProperties();
        val url = casProperties.getServer().getPrefix() + PalantirConstants.URL_PATH_PALANTIR + "/callback";
        sp.setService(url);
        sp.setSendRenew(false);
        return sp;
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirCasAuthenticationEntryPoint")
    public CasAuthenticationEntryPoint palantirCasAuthenticationEntryPoint(
        final CasConfigurationProperties casProperties,
        @Qualifier("palantirServiceProperties")
        final ServiceProperties serviceProperties) {
        val ep = new CasAuthenticationEntryPoint();
        ep.setLoginUrl(casProperties.getServer().getLoginUrl());
        ep.setServiceProperties(serviceProperties);
        return ep;
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirUserDetailsService")
    public UserDetailsService palantirUserDetailsService() {
        return username -> User.withUsername(username)
            .password("{noop}N/A")
            .authorities("ROLE_USER")
            .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirTicketValidator")
    public TicketValidator palantirTicketValidator(
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
        final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
        return new InternalTicketValidator(centralAuthenticationService,
            webApplicationServiceFactory,
            authenticationAttributeReleasePolicy, servicesManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirCasAuthenticationProvider")
    public CasAuthenticationProvider palantirCasAuthenticationProvider(
        final CasConfigurationProperties casProperties,
        @Qualifier("palantirServiceProperties")
        final ServiceProperties serviceProperties,
        @Qualifier("palantirTicketValidator")
        final TicketValidator palantirTicketValidator,
        @Qualifier("palantirUserDetailsService")
        final UserDetailsService userDetailsService) {
        val provider = new CasAuthenticationProvider();
        provider.setServiceProperties(serviceProperties);
        provider.setTicketValidator((ticket, service) -> FunctionUtils.doUnchecked(() -> {
            val validationResult = palantirTicketValidator.validate(ticket, service);
            val attributePrincipal = new AttributePrincipalImpl(validationResult.getPrincipal().getId(),
                (Map) validationResult.getPrincipal().getAttributes());
            return new AssertionImpl(attributePrincipal);
        }));
        provider.setAuthenticationUserDetailsService(new UserDetailsByNameServiceWrapper<>(userDetailsService));
        provider.setKey("cas-palantir-authentication-provider");
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirAthenticationManager")
    public AuthenticationManager palantirAthenticationManager(
        @Qualifier("palantirCasAuthenticationProvider")
        final CasAuthenticationProvider casAuthenticationProvider) {
        return new ProviderManager(casAuthenticationProvider);
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirCasAuthenticationFilter")
    public CasAuthenticationFilter palantirCasAuthenticationFilter(
        @Qualifier("palantirAthenticationManager")
        final AuthenticationManager palantirAthenticationManager,
        @Qualifier("palantirServiceProperties")
        final ServiceProperties serviceProperties) {
        val filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(palantirAthenticationManager);
        filter.setServiceProperties(serviceProperties);
        filter.setFilterProcessesUrl(PalantirConstants.URL_PATH_PALANTIR + "/callback");
        return filter;
    }
}
