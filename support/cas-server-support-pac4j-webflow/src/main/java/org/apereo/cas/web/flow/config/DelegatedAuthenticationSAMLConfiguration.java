package org.apereo.cas.web.flow.config;

import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;

import com.hazelcast.core.HazelcastInstance;
import org.pac4j.saml.store.HazelcastSAMLMessageStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Configuration(value = "DelegatedAuthenticationSAMLConfiguration", proxyBeanMethods = false)
public class DelegatedAuthenticationSAMLConfiguration {

    @ConditionalOnClass(value = HazelcastInstance.class)
    @Configuration(value = "DelegatedAuthenticationSAMLHazelcastConfiguration", proxyBeanMethods = false)
    public static class DelegatedAuthenticationSAMLHazelcastConfiguration {
        @Autowired
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnBean(name = "casTicketRegistryHazelcastInstance")
        @ConditionalOnMissingBean(name = DelegatedClientFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
        public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory(
            @Qualifier("casTicketRegistryHazelcastInstance") final ObjectProvider<HazelcastInstance> casTicketRegistryHazelcastInstance) {
            return new HazelcastSAMLMessageStoreFactory(casTicketRegistryHazelcastInstance.getObject());
        }
    }
}
