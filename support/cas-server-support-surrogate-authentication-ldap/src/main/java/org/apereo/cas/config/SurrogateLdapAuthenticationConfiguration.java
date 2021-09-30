package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateLdapAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

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

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "surrogateLdapConnectionFactory")
    @Autowired
    public ConnectionFactory surrogateLdapConnectionFactory(final CasConfigurationProperties casProperties) {
        val su = casProperties.getAuthn().getSurrogate();
        return LdapUtils.newLdaptiveConnectionFactory(su.getLdap());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public SurrogateAuthenticationService surrogateAuthenticationService(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("surrogateLdapConnectionFactory")
        final ConnectionFactory surrogateLdapConnectionFactory,
        final CasConfigurationProperties casProperties) {
        val su = casProperties.getAuthn().getSurrogate();
        LOGGER.debug("Using LDAP [{}] with baseDn [{}] to locate surrogate accounts",
            su.getLdap().getLdapUrl(), su.getLdap().getBaseDn());
        return new SurrogateLdapAuthenticationService(surrogateLdapConnectionFactory, su.getLdap(), servicesManager);
    }
}
