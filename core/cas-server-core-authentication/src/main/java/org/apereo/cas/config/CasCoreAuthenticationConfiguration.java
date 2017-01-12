package org.apereo.cas.config;

import org.apereo.cas.authentication.AllAuthenticationPolicy;
import org.apereo.cas.authentication.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.SuccessfulHandlerMetaDataPopulator;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.AuthenticationExceptionHandler;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CasCoreAuthenticationConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("geoLocationService")
    private GeoLocationService geoLocationService;


    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;


    @Bean
    public AuthenticationExceptionHandler authenticationExceptionHandler() {
        final AuthenticationExceptionHandler h = new AuthenticationExceptionHandler();
        h.setErrors(casProperties.getAuthn().getExceptions().getExceptions());
        return h;
    }

    @Bean(name = {"authenticationPolicy", "defaultAuthenticationPolicy"})
    public AuthenticationPolicy defaultAuthenticationPolicy() {
        final AuthenticationPolicyProperties police = casProperties.getAuthn().getPolicy();
        if (police.getReq().isEnabled()) {
            return new RequiredHandlerAuthenticationPolicy(police.getReq().getHandlerName(), police.getReq().isTryAll());
        }

        if (police.getAll().isEnabled()) {
            return new AllAuthenticationPolicy();
        }

        if (police.getNotPrevented().isEnabled()) {
            return new NotPreventedAuthenticationPolicy();
        }

        return new AnyAuthenticationPolicy(police.getAny().isTryAll());
    }


    @RefreshScope
    @Bean
    public AuthenticationContextValidator authenticationContextValidator() {
        final String contextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        final String failureMode = casProperties.getAuthn().getMfa().getGlobalFailureMode();
        final String authnAttributeName = casProperties.getAuthn().getMfa().getTrusted().getAuthenticationContextAttribute();
        return new DefaultAuthenticationContextValidator(contextAttribute, failureMode, authnAttributeName);
    }

    @Bean
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport() {
        return new DefaultAuthenticationSystemSupport(defaultAuthenticationTransactionManager(), defaultPrincipalElectionStrategy());
    }

    @Bean(name = {"defaultAuthenticationTransactionManager", "authenticationTransactionManager"})
    public AuthenticationTransactionManager defaultAuthenticationTransactionManager() {
        final DefaultAuthenticationTransactionManager r = new DefaultAuthenticationTransactionManager();
        r.setAuthenticationManager(authenticationManager());
        return r;
    }

    @Bean(name = {"defaultPrincipalElectionStrategy", "principalElectionStrategy"})
    public PrincipalElectionStrategy defaultPrincipalElectionStrategy() {
        final DefaultPrincipalElectionStrategy s = new DefaultPrincipalElectionStrategy();
        s.setPrincipalFactory(defaultPrincipalFactory());
        return s;
    }

    @ConditionalOnMissingBean(name = "principalFactory")
    @Bean(name = {"defaultPrincipalFactory", "principalFactory"})
    public PrincipalFactory defaultPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
    
    @Bean
    public AuthenticationPolicy notPreventedAuthenticationPolicy() {
        return new NotPreventedAuthenticationPolicy();
    }

    @Bean
    public List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators() {
        final List<AuthenticationMetaDataPopulator> list = new ArrayList<>();
        list.add(successfulHandlerMetaDataPopulator());
        list.add(rememberMeAuthenticationMetaDataPopulator());

        if (casProperties.getClearpass().isCacheCredential()) {
            list.add(new CacheCredentialsMetaDataPopulator());
        }
        return list;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new PolicyBasedAuthenticationManager(
                authenticationHandlersResolvers(),
                registeredServiceAuthenticationHandlerResolver(),
                authenticationMetadataPopulators(),
                defaultAuthenticationPolicy(),
                casProperties.getPersonDirectory().isPrincipalResolutionFailureFatal()
        );
    }

    @Bean
    public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver() {
        return new RegisteredServiceAuthenticationHandlerResolver(servicesManager);
    }

    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }

    @Bean
    public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
        return new SuccessfulHandlerMetaDataPopulator();
    }

    @Bean
    public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator() {
        return new RememberMeAuthenticationMetaDataPopulator();
    }
        
    @ConditionalOnMissingBean(name = "authenticationHandlersResolvers")
    @Bean
    public Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers() {
        return new TreeMap<>();
    }
        
    @Bean
    public AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy() {
        final DefaultAdaptiveAuthenticationPolicy p = new DefaultAdaptiveAuthenticationPolicy();
        p.setGeoLocationService(this.geoLocationService);
        p.setAdaptiveAuthenticationProperties(casProperties.getAuthn().getAdaptive());
        return p;
    }
}
