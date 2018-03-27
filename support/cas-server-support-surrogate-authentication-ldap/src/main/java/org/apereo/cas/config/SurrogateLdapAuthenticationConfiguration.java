package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.surrogate.SurrogateLdapAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateLdapAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("surrogateLdapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SurrogateLdapAuthenticationConfiguration {


    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
        LOGGER.debug("Using LDAP [{}] with baseDn [{}] to locate surrogate accounts",
                su.getLdap().getLdapUrl(), su.getLdap().getBaseDn());
        final ConnectionFactory factory = LdapUtils.newLdaptivePooledConnectionFactory(su.getLdap());
        return new SurrogateLdapAuthenticationService(factory, su.getLdap(), servicesManager);
    }
}
