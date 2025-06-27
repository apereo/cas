package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.hazelcast.core.HazelcastInstance;
import org.pac4j.saml.store.HazelcastSAMLMessageStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DelegatedAuthenticationSaml2HazelcastConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnClass(HazelcastInstance.class)
@Configuration(value = "DelegatedAuthenticationSaml2HazelcastConfiguration", proxyBeanMethods = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "hazelcast")
class DelegatedAuthenticationSaml2HazelcastConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnBean(name = "casTicketRegistryHazelcastInstance")
    @ConditionalOnMissingBean(name = DelegatedIdentityProviderFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
    public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory(
        @Qualifier("casTicketRegistryHazelcastInstance")
        final ObjectProvider<HazelcastInstance> casTicketRegistryHazelcastInstance) {
        return new HazelcastSAMLMessageStoreFactory(casTicketRegistryHazelcastInstance.getObject());
    }
}
