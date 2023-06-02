package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.passwordsync.LdapPasswordSynchronizationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.DisposableListFactoryBean;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link LdapPasswordSynchronizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.LDAP, module = "password-sync")
@AutoConfiguration
public class LdapPasswordSynchronizationConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.password-sync.enabled").isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public FactoryBean<List<Object>> ldapPasswordSynchronizationListFactoryBean() {
        return new DisposableListFactoryBean();
    }

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
        final CasConfigurationProperties casProperties,
        @Qualifier("ldapPasswordSynchronizationListFactoryBean")
        final FactoryBean<List<Object>> ldapPasswordSynchronizationListFactoryBean) {
        return BeanSupplier.of(BeanContainer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val postProcessorList = FunctionUtils.doUnchecked(ldapPasswordSynchronizationListFactoryBean::getObject);
                val ldap = casProperties.getAuthn().getPasswordSync().getLdap();
                val processors = ldap.stream()
                    .filter(LdapPasswordSynchronizationProperties::isEnabled)
                    .map(instance -> {
                        val postProcessor = new LdapPasswordSynchronizationAuthenticationPostProcessor(instance);
                        Objects.requireNonNull(postProcessorList).add(postProcessor);
                        return postProcessor;
                    })
                    .collect(Collectors.toList());
                return BeanContainer.of(processors);
            })
            .otherwise(BeanContainer::empty)
            .get();
    }
}
