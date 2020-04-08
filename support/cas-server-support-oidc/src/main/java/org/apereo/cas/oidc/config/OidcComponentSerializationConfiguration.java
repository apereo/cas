package org.apereo.cas.oidc.config;

import org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OidcComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "oidcComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcComponentSerializationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "oidcComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer oidcComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(PairwiseOidcRegisteredServiceUsernameAttributeProvider.class);
            plan.registerSerializableClass(OidcRegisteredService.class);
            plan.registerSerializableClass(OidcPairwisePersistentIdGenerator.class);

            plan.registerSerializableClass(OidcAddressScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcCustomScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcEmailScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcPhoneScopeAttributeReleasePolicy.class);
            plan.registerSerializableClass(OidcProfileScopeAttributeReleasePolicy.class);
        };
    }
}
