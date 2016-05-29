package org.apereo.cas.adaptors.ldap.services.config;

import org.apereo.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
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
public class LdapServiceRegistryConfiguration {
    
    @Bean
    @RefreshScope
    public LdapRegisteredServiceMapper ldapServiceRegistryMapper() {
        return new DefaultLdapRegisteredServiceMapper();
    }

    @Bean
    @RefreshScope
    public ServiceRegistryDao ldapServiceRegistryDao() {
        return new LdapServiceRegistryDao();
    }
    
}
