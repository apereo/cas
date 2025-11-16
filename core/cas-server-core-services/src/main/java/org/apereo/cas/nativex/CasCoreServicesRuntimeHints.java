package org.apereo.cas.nativex;

import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.AttributeBasedRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria;
import org.apereo.cas.services.AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.BaseRegisteredServiceAccessStrategy;
import org.apereo.cas.services.BaseRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.ChainingRegisteredServiceAttributeReleaseActivationCriteria;
import org.apereo.cas.services.ChainingRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.ChainingServiceRegistry;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceSurrogatePolicy;
import org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.FullRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.GroovyRegisteredServiceAccessStrategy;
import org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.GroovyRegisteredServiceAttributeReleaseActivationCriteria;
import org.apereo.cas.services.GroovyRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.GroovyRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.LiteralRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.NotPreventedRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.PatternMatchingAttributeReleasePolicy;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServicePasswordlessPolicy;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.RemoteEndpointServiceAccessStrategy;
import org.apereo.cas.services.RestfulRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnRestfulAttributeReleasePolicy;
import org.apereo.cas.services.ReturnStaticAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServiceRegistryInitializerEventListener;
import org.apereo.cas.services.StaticRegisteredServiceUsernameProvider;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.services.query.RegisteredServiceQueryIndex;
import org.apereo.cas.services.support.RegisteredServiceChainingAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceScriptedAttributeFilter;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CasCoreServicesRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreServicesRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, List.of(
            PrincipalProvisioner.class,
            ServiceRegistryInitializer.class,
            RegisteredServiceAccessStrategyEnforcer.class,
            ServiceRegistry.class,
            ServiceRegistryExecutionPlanConfigurer.class));

        registerSpringProxyHints(hints, ChainingServiceRegistry.class, ServiceRegistry.class);
        registerSerializableSpringProxyHints(hints, ServiceRegistryInitializerEventListener.class);

        registerSerializationHints(hints,
            BaseRegisteredService.class,
            BaseWebBasedRegisteredService.class,
            CasRegisteredService.class,

            BaseRegisteredServiceAccessStrategy.class,
            AbstractRegisteredServiceAttributeReleasePolicy.class,

            RegisteredServiceLogoutType.class,
            RegisteredServicePublicKeyImpl.class,
            DefaultRegisteredServiceContact.class,
            DefaultRegisteredServiceProperty.class,
            DefaultRegisteredServiceDelegatedAuthenticationPolicy.class,
            ChainingRegisteredServiceDelegatedAuthenticationPolicy.class,
            ChainingRegisteredServiceAttributeReleaseActivationCriteria.class,
            DefaultRegisteredServiceExpirationPolicy.class,
            DefaultRegisteredServiceServiceTicketExpirationPolicy.class,
            DefaultRegisteredServiceProxyTicketExpirationPolicy.class,
            DefaultRegisteredServiceDelegatedAuthenticationPolicy.class,
            DefaultRegisteredServiceAcceptableUsagePolicy.class,
            DefaultRegisteredServiceAuthenticationPolicy.class,
            ShibbolethCompatiblePersistentIdGenerator.class,
            FullRegexRegisteredServiceMatchingStrategy.class,
            PartialRegexRegisteredServiceMatchingStrategy.class,
            LiteralRegisteredServiceMatchingStrategy.class,

            RegexMatchingRegisteredServiceProxyPolicy.class,
            RefuseRegisteredServiceProxyPolicy.class,
            DefaultRegisteredServiceAccessStrategy.class,
            ChainingRegisteredServiceAccessStrategy.class,
            RemoteEndpointServiceAccessStrategy.class,
            TimeBasedRegisteredServiceAccessStrategy.class,
            DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy.class,
            DefaultRegisteredServiceProxyTicketExpirationPolicy.class,
            DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy.class,
            DefaultRegisteredServiceServiceTicketExpirationPolicy.class,
            PrincipalAttributeRegisteredServiceUsernameProvider.class,
            AnonymousRegisteredServiceUsernameAttributeProvider.class,
            BaseRegisteredServiceUsernameAttributeProvider.class,
            StaticRegisteredServiceUsernameProvider.class,
            DefaultRegisteredServiceUsernameProvider.class,
            DefaultRegisteredServiceWebflowInterruptPolicy.class,
            RegisteredServiceRegexAttributeFilter.class,
            RegisteredServiceChainingAttributeFilter.class,
            RegisteredServiceMappedRegexAttributeFilter.class,
            RegisteredServiceScriptedAttributeFilter.class,
            ChainingRegisteredServiceAccessStrategyActivationCriteria.class,
            AttributeBasedRegisteredServiceAccessStrategyActivationCriteria.class,
            AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria.class,
            AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy.class,
            DefaultRegisteredServiceSurrogatePolicy.class,

            ChainingAttributeReleasePolicy.class,
            DenyAllAttributeReleasePolicy.class,
            ReturnAllowedAttributeReleasePolicy.class,
            ReturnAllAttributeReleasePolicy.class,
            ReturnStaticAttributeReleasePolicy.class,
            ReturnMappedAttributeReleasePolicy.class,
            ReturnRestfulAttributeReleasePolicy.class,
            PatternMatchingAttributeReleasePolicy.class,
            PatternMatchingAttributeReleasePolicy.Rule.class,

            DefaultRegisteredServiceMultifactorPolicy.class,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.class,

            AbstractPrincipalAttributesRepository.class,
            CachingPrincipalAttributesRepository.class,
            DefaultPrincipalAttributesRepository.class,
            PrincipalAttributesCoreProperties.MergingStrategyTypes.class,

            DefaultRegisteredServiceConsentPolicy.class,

            AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria.class,
            AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria.class,
            NotPreventedRegisteredServiceAuthenticationPolicyCriteria.class,
            RestfulRegisteredServiceAuthenticationPolicyCriteria.class
        );

        if (isGroovyPresent(classLoader)) {
            registerSerializationHints(hints, List.of(
                GroovyRegisteredServiceAccessStrategy.class,
                GroovyRegisteredServiceUsernameProvider.class,
                GroovyRegisteredServiceAttributeReleaseActivationCriteria.class,
                GroovyRegisteredServiceAccessStrategyActivationCriteria.class,
                GroovyRegisteredServiceSingleSignOnParticipationPolicy.class,
                GroovyScriptAttributeReleasePolicy.class,
                GroovyRegisteredServiceAuthenticationPolicyCriteria.class
            ));
        }
        registerReflectionHintsForDeclaredAndPublicElements(hints, List.of(
            UnauthorizedServiceException.class,
            RegisteredServiceQuery.class,
            RegisteredServiceQueryIndex.class,
            CasRegisteredService.class
        ));

        val classes = findSubclassesOf(RegisteredServicePasswordlessPolicy.class);
        registerSerializationHints(hints, classes);
        registerReflectionHints(hints, classes);
    }
}
