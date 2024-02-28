package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateLdapAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSurrogateLdapAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication, module = "ldap")
@AutoConfiguration
public class CasSurrogateLdapAuthenticationAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.surrogate.ldap.ldap-url");

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "surrogateLdapConnectionFactory")
    public ConnectionFactory surrogateLdapConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(ConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val su = casProperties.getAuthn().getSurrogate();
                return LdapUtils.newLdaptiveConnectionFactory(su.getLdap());
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ldapSurrogateAuthenticationService")
    public BeanSupplier<SurrogateAuthenticationService> ldapSurrogateAuthenticationService(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("surrogateLdapConnectionFactory")
        final ConnectionFactory surrogateLdapConnectionFactory,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(SurrogateAuthenticationService.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val su = casProperties.getAuthn().getSurrogate();
                LOGGER.debug("Using LDAP [{}] with baseDn [{}] to locate surrogate accounts",
                    su.getLdap().getLdapUrl(), su.getLdap().getBaseDn());
                return new SurrogateLdapAuthenticationService(surrogateLdapConnectionFactory, su.getLdap(), servicesManager);
            })
            .otherwiseNull();
    }
}
