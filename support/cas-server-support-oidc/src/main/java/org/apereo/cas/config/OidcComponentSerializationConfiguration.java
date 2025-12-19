package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcAssuranceScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcDefaultCibaRequest;
import org.apereo.cas.oidc.ticket.OidcDefaultPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OidcComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@Configuration(value = "OidcComponentSerializationConfiguration", proxyBeanMethods = false)
class OidcComponentSerializationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "oidcTicketSerializationExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketSerializationExecutionPlanConfigurer oidcTicketSerializationExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext) {
        return plan -> {
            plan.registerTicketSerializer(new OidcPushedAuthorizationRequestSerializer(applicationContext));
            plan.registerTicketSerializer(new OidcCibaRequestSerializer(applicationContext));

            plan.registerTicketSerializer(OidcPushedAuthorizationRequest.class.getName(), new OidcPushedAuthorizationRequestSerializer(applicationContext));
            plan.registerTicketSerializer(OidcCibaRequest.class.getName(), new OidcCibaRequestSerializer(applicationContext));
            plan.registerTicketSerializer(OidcPushedAuthorizationRequest.PREFIX, new OidcPushedAuthorizationRequestSerializer(applicationContext));
            plan.registerTicketSerializer(OidcCibaRequest.PREFIX, new OidcCibaRequestSerializer(applicationContext));
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcComponentSerializationPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer oidcComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(PairwiseOidcRegisteredServiceUsernameAttributeProvider.class);
            plan.registerSerializableClass(OidcRegisteredService.class);
            plan.registerSerializableClass(OidcPairwisePersistentIdGenerator.class);
            plan.registerSerializableClass(OidcDefaultPushedAuthorizationRequest.class);

            plan.registerSerializableClass(OidcAddressScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcCustomScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcEmailScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcPhoneScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcAssuranceScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcScopeFreeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcProfileScopeAttributeReleasePolicy.class);
        };
    }

    private static final class OidcPushedAuthorizationRequestSerializer extends
        BaseJacksonSerializer<OidcDefaultPushedAuthorizationRequest> {
        @Serial
        private static final long serialVersionUID = -6298623586274810263L;

        OidcPushedAuthorizationRequestSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, OidcDefaultPushedAuthorizationRequest.class);
        }
    }

    private static final class OidcCibaRequestSerializer extends
        BaseJacksonSerializer<OidcDefaultCibaRequest> {
        @Serial
        private static final long serialVersionUID = -1298623586274810263L;

        OidcCibaRequestSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, OidcDefaultCibaRequest.class);
        }
    }
}
