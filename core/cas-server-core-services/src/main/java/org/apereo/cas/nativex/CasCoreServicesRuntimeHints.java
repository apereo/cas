package org.apereo.cas.nativex;

import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
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
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.BaseRegisteredServiceAccessStrategy;
import org.apereo.cas.services.BaseRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.ChainingRegisteredServiceDelegatedAuthenticationPolicy;
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
import org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.FullRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.GroovyRegisteredServiceAccessStrategy;
import org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.GroovyRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.LiteralRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.NotPreventedRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.PatternMatchingAttributeReleasePolicy;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceLogoutType;
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
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

/**
 * This is {@link CasCoreServicesRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreServicesRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies()
            .registerJdkProxy(ServiceRegistryInitializer.class)
            .registerJdkProxy(RegisteredServiceAccessStrategyEnforcer.class)
            .registerJdkProxy(ServiceRegistry.class)
            .registerJdkProxy(ServiceRegistryExecutionPlanConfigurer.class);

        registerSerializableSpringProxy(hints, ServiceRegistryInitializerEventListener.class);

        hints.serialization()
            .registerType(BaseRegisteredService.class)
            .registerType(BaseWebBasedRegisteredService.class)
            .registerType(CasRegisteredService.class)
            .registerType(RegexRegisteredService.class)

            .registerType(BaseRegisteredServiceAccessStrategy.class)
            .registerType(AbstractRegisteredServiceAttributeReleasePolicy.class)

            .registerType(RegisteredServiceLogoutType.class)
            .registerType(RegisteredServicePublicKeyImpl.class)
            .registerType(DefaultRegisteredServiceContact.class)
            .registerType(DefaultRegisteredServiceProperty.class)
            .registerType(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class)
            .registerType(ChainingRegisteredServiceDelegatedAuthenticationPolicy.class)
            .registerType(DefaultRegisteredServiceExpirationPolicy.class)
            .registerType(DefaultRegisteredServiceServiceTicketExpirationPolicy.class)
            .registerType(DefaultRegisteredServiceProxyTicketExpirationPolicy.class)
            .registerType(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class)
            .registerType(DefaultRegisteredServiceAcceptableUsagePolicy.class)
            .registerType(DefaultRegisteredServiceAuthenticationPolicy.class)
            .registerType(ShibbolethCompatiblePersistentIdGenerator.class)
            .registerType(FullRegexRegisteredServiceMatchingStrategy.class)
            .registerType(PartialRegexRegisteredServiceMatchingStrategy.class)
            .registerType(LiteralRegisteredServiceMatchingStrategy.class)

            .registerType(RegexMatchingRegisteredServiceProxyPolicy.class)
            .registerType(RefuseRegisteredServiceProxyPolicy.class)
            .registerType(DefaultRegisteredServiceAccessStrategy.class)
            .registerType(ChainingRegisteredServiceAccessStrategy.class)
            .registerType(GroovyRegisteredServiceAccessStrategy.class)
            .registerType(RemoteEndpointServiceAccessStrategy.class)
            .registerType(TimeBasedRegisteredServiceAccessStrategy.class)
            .registerType(DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy.class)
            .registerType(DefaultRegisteredServiceProxyTicketExpirationPolicy.class)
            .registerType(DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy.class)
            .registerType(DefaultRegisteredServiceServiceTicketExpirationPolicy.class)
            .registerType(PrincipalAttributeRegisteredServiceUsernameProvider.class)
            .registerType(AnonymousRegisteredServiceUsernameAttributeProvider.class)
            .registerType(BaseRegisteredServiceUsernameAttributeProvider.class)
            .registerType(StaticRegisteredServiceUsernameProvider.class)
            .registerType(GroovyRegisteredServiceUsernameProvider.class)
            .registerType(DefaultRegisteredServiceUsernameProvider.class)
            .registerType(DefaultRegisteredServiceWebflowInterruptPolicy.class)
            .registerType(RegisteredServiceRegexAttributeFilter.class)
            .registerType(RegisteredServiceChainingAttributeFilter.class)
            .registerType(RegisteredServiceMappedRegexAttributeFilter.class)
            .registerType(RegisteredServiceScriptedAttributeFilter.class)
            .registerType(ChainingRegisteredServiceAccessStrategyActivationCriteria.class)
            .registerType(AttributeBasedRegisteredServiceAccessStrategyActivationCriteria.class)
            .registerType(GroovyRegisteredServiceAccessStrategyActivationCriteria.class)

            .registerType(ChainingAttributeReleasePolicy.class)
            .registerType(DenyAllAttributeReleasePolicy.class)
            .registerType(ReturnAllowedAttributeReleasePolicy.class)
            .registerType(ReturnAllAttributeReleasePolicy.class)
            .registerType(ReturnStaticAttributeReleasePolicy.class)
            .registerType(ReturnMappedAttributeReleasePolicy.class)
            .registerType(GroovyScriptAttributeReleasePolicy.class)
            .registerType(ReturnRestfulAttributeReleasePolicy.class)
            .registerType(PatternMatchingAttributeReleasePolicy.class)
            .registerType(PatternMatchingAttributeReleasePolicy.Rule.class)

            .registerType(DefaultRegisteredServiceMultifactorPolicy.class)
            .registerType(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.class)

            .registerType(AbstractPrincipalAttributesRepository.class)
            .registerType(CachingPrincipalAttributesRepository.class)
            .registerType(DefaultPrincipalAttributesRepository.class)
            .registerType(PrincipalAttributesCoreProperties.MergingStrategyTypes.class)

            .registerType(DefaultRegisteredServiceConsentPolicy.class)

            .registerType(AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria.class)
            .registerType(AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria.class)
            .registerType(GroovyRegisteredServiceAuthenticationPolicyCriteria.class)
            .registerType(NotPreventedRegisteredServiceAuthenticationPolicyCriteria.class)
            .registerType(RestfulRegisteredServiceAuthenticationPolicyCriteria.class);

        List.of(
            UnauthorizedServiceException.class,
            RegisteredServiceQuery.class,
            RegisteredServiceQueryIndex.class,
            CasRegisteredService.class
        ).forEach(el ->
            hints.reflection().registerType(TypeReference.of(el),
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }
}
