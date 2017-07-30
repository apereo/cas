package org.apereo.cas.adaptors.ldap.services.config;

import org.apereo.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapServiceRegistryDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;

import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link LdapServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapServiceRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "ldapServiceRegistryMapper")
    public LdapRegisteredServiceMapper ldapServiceRegistryMapper() {
        return new DefaultLdapRegisteredServiceMapper(casProperties.getServiceRegistry().getLdap());
    }

    @Bean
    @RefreshScope
    @Autowired
    public ServiceRegistryDao serviceRegistryDao(@Qualifier("ldapServiceRegistryMapper") final LdapRegisteredServiceMapper mapper) {
        final LdapServiceRegistryProperties ldap = casProperties.getServiceRegistry().getLdap();
        final ConnectionFactory connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
        return new LdapServiceRegistryDao(connectionFactory, ldap.getBaseDn(), mapper, ldap);
    }
}
