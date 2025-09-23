package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateLdapAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
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
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.surrogate.ldap[0].ldap-url");

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ldapSurrogateAuthenticationService")
    public BeanSupplier<SurrogateAuthenticationService> ldapSurrogateAuthenticationService(
        @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
        final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(SurrogateAuthenticationService.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new SurrogateLdapAuthenticationService(casProperties,
                servicesManager, principalAccessStrategyEnforcer, applicationContext))
            .otherwiseNull();
    }
}
