package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
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
import org.apereo.cas.services.GroovyRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.GroovyRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.LiteralRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.NotPreventedRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.RemoteEndpointServiceAccessStrategy;
import org.apereo.cas.services.RestfulRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnRestfulAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceUsernameProvider;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceChainingAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceScriptedAttributeFilter;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreServicesComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casCoreServicesComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreServicesComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casCoreServicesComponentSerializationPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer casCoreServicesComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(RegexRegisteredService.class);
            plan.registerSerializableClass(RegisteredServiceLogoutType.class);
            plan.registerSerializableClass(RegisteredServicePublicKeyImpl.class);
            plan.registerSerializableClass(DefaultRegisteredServiceContact.class);
            plan.registerSerializableClass(DefaultRegisteredServiceProperty.class);
            plan.registerSerializableClass(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceExpirationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceServiceTicketExpirationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceProxyTicketExpirationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceAcceptableUsagePolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceAuthenticationPolicy.class);
            plan.registerSerializableClass(ShibbolethCompatiblePersistentIdGenerator.class);
            plan.registerSerializableClass(FullRegexRegisteredServiceMatchingStrategy.class);
            plan.registerSerializableClass(PartialRegexRegisteredServiceMatchingStrategy.class);
            plan.registerSerializableClass(LiteralRegisteredServiceMatchingStrategy.class);

            plan.registerSerializableClass(RegexMatchingRegisteredServiceProxyPolicy.class);
            plan.registerSerializableClass(RefuseRegisteredServiceProxyPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceAccessStrategy.class);
            plan.registerSerializableClass(GroovyRegisteredServiceAccessStrategy.class);
            plan.registerSerializableClass(RemoteEndpointServiceAccessStrategy.class);
            plan.registerSerializableClass(TimeBasedRegisteredServiceAccessStrategy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceProxyTicketExpirationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy.class);
            plan.registerSerializableClass(DefaultRegisteredServiceServiceTicketExpirationPolicy.class);
            plan.registerSerializableClass(PrincipalAttributeRegisteredServiceUsernameProvider.class);
            plan.registerSerializableClass(AnonymousRegisteredServiceUsernameAttributeProvider.class);
            plan.registerSerializableClass(GroovyRegisteredServiceUsernameProvider.class);
            plan.registerSerializableClass(DefaultRegisteredServiceUsernameProvider.class);
            plan.registerSerializableClass(DefaultRegisteredServiceWebflowInterruptPolicy.class);
            plan.registerSerializableClass(ScriptedRegisteredServiceUsernameProvider.class);
            plan.registerSerializableClass(RegisteredServiceRegexAttributeFilter.class);
            plan.registerSerializableClass(RegisteredServiceChainingAttributeFilter.class);
            plan.registerSerializableClass(RegisteredServiceMappedRegexAttributeFilter.class);
            plan.registerSerializableClass(RegisteredServiceScriptedAttributeFilter.class);

            plan.registerSerializableClass(ChainingAttributeReleasePolicy.class);
            plan.registerSerializableClass(DenyAllAttributeReleasePolicy.class);
            plan.registerSerializableClass(ReturnAllowedAttributeReleasePolicy.class);
            plan.registerSerializableClass(ReturnAllAttributeReleasePolicy.class);
            plan.registerSerializableClass(ReturnMappedAttributeReleasePolicy.class);
            plan.registerSerializableClass(GroovyScriptAttributeReleasePolicy.class);
            plan.registerSerializableClass(ScriptedRegisteredServiceAttributeReleasePolicy.class);
            plan.registerSerializableClass(ReturnRestfulAttributeReleasePolicy.class);

            plan.registerSerializableClass(DefaultRegisteredServiceMultifactorPolicy.class);
            plan.registerSerializableClass(GroovyRegisteredServiceMultifactorPolicy.class);
            plan.registerSerializableClass(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.class);

            plan.registerSerializableClass(CachingPrincipalAttributesRepository.class);
            plan.registerSerializableClass(DefaultPrincipalAttributesRepository.class);
            plan.registerSerializableClass(PrincipalAttributesCoreProperties.MergingStrategyTypes.class);

            plan.registerSerializableClass(DefaultRegisteredServiceConsentPolicy.class);

            plan.registerSerializableClass(AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria.class);
            plan.registerSerializableClass(AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria.class);
            plan.registerSerializableClass(GroovyRegisteredServiceAuthenticationPolicyCriteria.class);
            plan.registerSerializableClass(NotPreventedRegisteredServiceAuthenticationPolicyCriteria.class);
            plan.registerSerializableClass(RestfulRegisteredServiceAuthenticationPolicyCriteria.class);
        };
    }
}
