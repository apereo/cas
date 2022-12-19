package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import com.hazelcast.core.HazelcastInstance;
import org.pac4j.saml.store.HazelcastSAMLMessageStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DelegatedAuthenticationSAMLConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml")
@AutoConfiguration
public class DelegatedAuthenticationSAMLConfiguration {

    @ConditionalOnClass(HazelcastInstance.class)
    @Configuration(value = "DelegatedAuthenticationSAMLHazelcastConfiguration", proxyBeanMethods = false)
    public static class DelegatedAuthenticationSAMLHazelcastConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnBean(name = "casTicketRegistryHazelcastInstance")
        @ConditionalOnMissingBean(name = DelegatedClientFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
        public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory(
            @Qualifier("casTicketRegistryHazelcastInstance")
            final ObjectProvider<HazelcastInstance> casTicketRegistryHazelcastInstance) {
            return new HazelcastSAMLMessageStoreFactory(casTicketRegistryHazelcastInstance.getObject());
        }
    }
}
