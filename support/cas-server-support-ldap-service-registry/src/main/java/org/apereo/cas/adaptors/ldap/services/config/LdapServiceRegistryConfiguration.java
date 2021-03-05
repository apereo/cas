package org.apereo.cas.adaptors.ldap.services.config;

import org.apereo.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapServiceRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.LdapUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link LdapServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapServiceRegistryConfiguration")
@ConditionalOnProperty(prefix = "cas.service-registry.ldap", name = "ldap-url")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class LdapServiceRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "ldapServiceRegistryMapper")
    public LdapRegisteredServiceMapper ldapServiceRegistryMapper() {
        return new DefaultLdapRegisteredServiceMapper(casProperties.getServiceRegistry().getLdap());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "ldapServiceRegistry")
    public ServiceRegistry ldapServiceRegistry() {
        val ldap = casProperties.getServiceRegistry().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        LOGGER.debug("Configured LDAP service registry search filter to [{}] and load filter to [{}]",
            ldap.getSearchFilter(), ldap.getLoadFilter());
        return new LdapServiceRegistry(connectionFactory, ldapServiceRegistryMapper(),
            ldap, applicationContext, serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "ldapServiceRegistryExecutionPlanConfigurer")
    @RefreshScope
    public ServiceRegistryExecutionPlanConfigurer ldapServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(ldapServiceRegistry());
    }
}
