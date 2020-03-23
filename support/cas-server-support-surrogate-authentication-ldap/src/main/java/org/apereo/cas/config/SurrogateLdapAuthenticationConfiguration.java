package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateLdapAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration(value = "surrogateLdapAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SurrogateLdapAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        val su = casProperties.getAuthn().getSurrogate();
        LOGGER.debug("Using LDAP [{}] with baseDn [{}] to locate surrogate accounts",
            su.getLdap().getLdapUrl(), su.getLdap().getBaseDn());
        val factory = LdapUtils.newLdaptiveConnectionFactory(su.getLdap());
        return new SurrogateLdapAuthenticationService(factory, su.getLdap(), servicesManager.getObject());
    }
}
