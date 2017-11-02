package org.apereo.cas.web.flow.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.webapp.WebflowProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerAction;
import org.apereo.cas.web.flow.actions.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.actions.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.actions.InjectResponseHeadersAction;
import org.apereo.cas.web.flow.actions.RedirectToServiceAction;
import org.apereo.cas.web.flow.authentication.GroovyScriptMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.InitialAuthenticationAttemptWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RankedAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.SelectiveAuthenticationProviderWebflowEventEventResolver;
import org.apereo.cas.web.flow.resolver.impl.ServiceTicketRequestWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.AuthenticationAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.GlobalMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.GroovyScriptMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.PrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RegisteredServiceMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RequestParameterMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RestEndpointMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.adaptive.AdaptiveMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.adaptive.TimedMultifactorAuthenticationPolicyEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Action;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link CasCoreWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreWebflowConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreWebflowConfiguration.class);

    @Autowired(required = false)
    @Qualifier("geoLocationService")
    private GeoLocationService geoLocationService;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("webApplicationResponseBuilderLocator")
    private ResponseBuilderLocator responseBuilderLocator;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector selector;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver() {
        return new AdaptiveMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
                selector, casProperties, geoLocationService);
    }

    @ConditionalOnMissingBean(name = "timedAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver() {
        return new TimedMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
                selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "principalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new PrincipalAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver() {
        return new PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }


    @ConditionalOnMissingBean(name = "authenticationAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver() {
        return new AuthenticationAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies,
                selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "multifactorAuthenticationProviderSelector")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector() {
        final Resource script = casProperties.getAuthn().getMfa().getProviderSelectorGroovyScript();
        if (script != null) {
            return new GroovyScriptMultifactorAuthenticationProviderSelector(script);
        }

        return new RankedMultifactorAuthenticationProviderSelector();
    }

    @ConditionalOnMissingBean(name = "initialAuthenticationAttemptWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver() {
        final InitialAuthenticationAttemptWebflowEventResolver r = new InitialAuthenticationAttemptWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
                selector);
        r.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(timedAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(globalAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(requestParameterAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(groovyScriptAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver());
        r.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(authenticationAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver());
        r.setSelectiveResolver(selectiveAuthenticationProviderWebflowEventResolver());
        return r;
    }

    @ConditionalOnMissingBean(name = "restEndpointAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver() {
        return new RestEndpointMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "serviceTicketRequestWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver() {
        return new ServiceTicketRequestWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver() {
        return new GlobalMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "groovyScriptAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver() {
        return new GroovyScriptMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "selectiveAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver() {
        return new SelectiveAuthenticationProviderWebflowEventEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "requestParameterAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver requestParameterAuthenticationPolicyWebflowEventResolver() {
        return new RequestParameterMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver() {
        return new RegisteredServiceMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "rankedAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver() {
        return new RankedAuthenticationProviderWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies,
                selector, authenticationContextValidator,
                initialAuthenticationAttemptWebflowEventResolver());
    }

    @Bean
    @RefreshScope
    public CipherExecutor webflowCipherExecutor() {
        final WebflowProperties webflow = casProperties.getWebflow();
        final EncryptionRandomizedSigningJwtCryptographyProperties crypto = webflow.getCrypto();

        boolean enabled = crypto.isEnabled();
        if (!enabled && (StringUtils.isNotBlank(crypto.getEncryption().getKey())) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
            LOGGER.warn("Webflow encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
                    + "are defined for operations. CAS will proceed to enable the webflow encryption/signing functionality.");
            enabled = true;
        }
        if (enabled) {
            return new WebflowConversationStateCipherExecutor(
                    crypto.getEncryption().getKey(),
                    crypto.getSigning().getKey(),
                    crypto.getAlg(),
                    crypto.getSigning().getKeySize(),
                    crypto.getEncryption().getKeySize());
        }
        LOGGER.warn("Webflow encryption/signing is turned off. This "
                + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
                + "signing and verification of webflow state.");
        return NoOpCipherExecutor.getInstance();
    }

    @Bean
    @ConditionalOnMissingBean(name = "clearWebflowCredentialsAction")
    @RefreshScope
    public Action clearWebflowCredentialsAction() {
        return new ClearWebflowCredentialAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "checkWebAuthenticationRequestAction")
    @RefreshScope
    public Action checkWebAuthenticationRequestAction() {
        return new CheckWebAuthenticationRequestAction(casProperties.getAuthn().getMfa().getContentType());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redirectToServiceAction")
    @RefreshScope
    public Action redirectToServiceAction() {
        return new RedirectToServiceAction(responseBuilderLocator);
    }

    @Bean
    @ConditionalOnMissingBean(name = "injectResponseHeadersAction")
    @RefreshScope
    public Action injectResponseHeadersAction() {
        return new InjectResponseHeadersAction(responseBuilderLocator);
    }

    @Bean
    @ConditionalOnMissingBean(name = "singleSignOnParticipationStrategy")
    @RefreshScope
    public SingleSignOnParticipationStrategy singleSignOnParticipationStrategy() {
        return new DefaultSingleSignOnParticipationStrategy(servicesManager, casProperties.getSso().isRenewedAuthn());
    }

    @ConditionalOnMissingBean(name = "authenticationExceptionHandler")
    @Bean
    public Action authenticationExceptionHandler() {
        return new AuthenticationExceptionHandlerAction(handledAuthenticationExceptions());
    }

    @RefreshScope
    @Bean
    public Set<Class<? extends Exception>> handledAuthenticationExceptions() {
        /*
         * Order is important here; We want the account policy exceptions to be handled
         * first before moving onto more generic errors. In the event that multiple handlers
         * are defined, where one failed due to account policy restriction and one fails
         * due to a bad password, we want the error associated with the account policy
         * to be processed first, rather than presenting a more generic error associated
         */
        final Set<Class<? extends Exception>> errors = new LinkedHashSet<>();
        errors.add(javax.security.auth.login.AccountLockedException.class);
        errors.add(javax.security.auth.login.CredentialExpiredException.class);
        errors.add(javax.security.auth.login.AccountExpiredException.class);
        errors.add(AccountDisabledException.class);
        errors.add(InvalidLoginLocationException.class);
        errors.add(AccountPasswordMustChangeException.class);
        errors.add(InvalidLoginTimeException.class);

        errors.add(javax.security.auth.login.AccountNotFoundException.class);
        errors.add(javax.security.auth.login.FailedLoginException.class);
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(PrincipalException.class);
        errors.add(UnsatisfiedAuthenticationPolicyException.class);
        errors.add(UnauthorizedAuthenticationException.class);

        errors.addAll(casProperties.getAuthn().getExceptions().getExceptions());

        return errors;
    }
}
