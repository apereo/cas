package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.heimdall.HeimdallAuthorizationController;
import org.apereo.cas.heimdall.HeimdallAuthorizationEndpoint;
import org.apereo.cas.heimdall.authorizer.DefaultResourceAuthorizer;
import org.apereo.cas.heimdall.authorizer.ResourceAuthorizer;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.repository.JsonAuthorizableResourceRepository;
import org.apereo.cas.heimdall.engine.AuthorizationEngine;
import org.apereo.cas.heimdall.engine.AuthorizationPrincipalParser;
import org.apereo.cas.heimdall.engine.DefaultAuthorizationEngine;
import org.apereo.cas.heimdall.engine.DefaultAuthorizationPrincipalParser;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.List;

/**
 * This is {@link CasHeimdallAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authorization)
@AutoConfiguration
public class CasHeimdallAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "authorizationPrincipalParser")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthorizationPrincipalParser authorizationPrincipalParser(
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        final CasConfigurationProperties casProperties,
        @Qualifier("oidcTokenSigningAndEncryptionService")
        final ObjectProvider<OAuth20TokenSigningAndEncryptionService> oidcTokenSigningAndEncryptionService,
        @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
        final ObjectProvider<JwtBuilder> accessTokenJwtBuilder,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return new DefaultAuthorizationPrincipalParser(ticketRegistry, casProperties,
            accessTokenJwtBuilder, oidcTokenSigningAndEncryptionService, authenticationSystemSupport);
    }

    @Bean
    @ConditionalOnMissingBean(name = "heimdallAuthorizationController")
    public HeimdallAuthorizationController heimdallAuthorizationController(
        @Qualifier("authorizationPrincipalParser")
        final AuthorizationPrincipalParser authorizationPrincipalParser,
        @Qualifier("heimdallAuthorizationEngine")
        final AuthorizationEngine heimdallAuthorizationEngine) {
        return new HeimdallAuthorizationController(heimdallAuthorizationEngine, authorizationPrincipalParser);
    }

    @Bean
    @ConditionalOnMissingBean(name = "heimdallAuthorizationEngine")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthorizationEngine heimdallAuthorizationEngine(
        final List<ResourceAuthorizer> resourceAuthorizers,
        @Qualifier(AuthorizableResourceRepository.BEAN_NAME)
        final AuthorizableResourceRepository authorizableResourceRepository) {
        return new DefaultAuthorizationEngine(authorizableResourceRepository, resourceAuthorizers);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = AuthorizableResourceRepository.BEAN_NAME)
    public AuthorizableResourceRepository authorizableResourceRepository(
        final CasConfigurationProperties casProperties) throws Exception {
        val location = casProperties.getHeimdall().getJson().getLocation();
        return BeanSupplier.of(AuthorizableResourceRepository.class)
            .when(() -> location != null)
            .supplyUnchecked(() -> new JsonAuthorizableResourceRepository(location.getFile()))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "defaultResourceAuthorizer")
    public ResourceAuthorizer defaultResourceAuthorizer() {
        return new DefaultResourceAuthorizer();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "heimdallEndpointConfigurer")
    public CasWebSecurityConfigurer<Void> heimdallEndpointConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of(HeimdallAuthorizationController.BASE_URL);
            }
        };
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnAvailableEndpoint
    public HeimdallAuthorizationEndpoint heimdallAuthorizationEndpoint(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(AuthorizableResourceRepository.BEAN_NAME)
        final AuthorizableResourceRepository authorizableResourceRepository) {
        return new HeimdallAuthorizationEndpoint(casProperties,
            applicationContext, authorizableResourceRepository);
    }

}
