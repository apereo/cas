package org.apereo.cas.adaptors.ldap.services.config;

import org.apereo.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapServiceRegistryDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistryDao;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

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


    @Nullable
    @Autowired(required = false)
    @Qualifier("ldapServiceRegistryConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Bean
    @RefreshScope
    public LdapRegisteredServiceMapper ldapServiceRegistryMapper() {
        return new DefaultLdapRegisteredServiceMapper();
    }

    @Bean(name = {"ldapServiceRegistryDao", "serviceRegistryDao"})
    @RefreshScope
    public ServiceRegistryDao ldapServiceRegistryDao() {
        final LdapServiceRegistryDao r = new LdapServiceRegistryDao();

        r.setConnectionFactory(connectionFactory);
        r.setLdapServiceMapper(ldapServiceRegistryMapper());
        r.setBaseDn(casProperties.getServiceRegistry().getLdap().getBaseDn());
        
        return r;
    }

}
