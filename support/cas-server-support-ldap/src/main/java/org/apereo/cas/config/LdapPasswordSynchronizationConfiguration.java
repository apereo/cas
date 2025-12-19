package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.passwordsync.LdapPasswordSynchronizationProperties;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link LdapPasswordSynchronizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.LDAP, module = "password-sync")
@Configuration(value = "LdapPasswordSynchronizationConfiguration", proxyBeanMethods = false)
class LdapPasswordSynchronizationConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.password-sync.enabled").isTrue().evenIfMissing();
    
    @ConditionalOnMissingBean(name = "ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("ldapPasswordSynchronizers")
        final BeanContainer<AuthenticationPostProcessor> ldapPasswordSynchronizers,
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> ldapPasswordSynchronizers.forEach(plan::registerAuthenticationPostProcessor))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "ldapPasswordSynchronizers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BeanContainer<AuthenticationPostProcessor> ldapPasswordSynchronizers(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(BeanContainer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val ldap = casProperties.getAuthn().getPasswordSync().getLdap();
                val processors = ldap.stream()
                    .filter(LdapPasswordSynchronizationProperties::isEnabled)
                    .map(properties -> {
                        val searchFactory = new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(properties));
                        return new LdapPasswordSynchronizationAuthenticationPostProcessor(searchFactory, properties);
                    })
                    .collect(Collectors.toList());
                return BeanContainer.of(processors);
            })
            .otherwise(BeanContainer::empty)
            .get();
    }
}
