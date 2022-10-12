package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.GroovyDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.RestfulDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.ScimDelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.scim.v2.ScimV2PrincipalAttributeMapper;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.function.Supplier;

/**
 * This is {@link Pac4jAuthenticationProvisioningConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@AutoConfiguration
public class Pac4jAuthenticationProvisioningConfiguration {
    @Configuration(value = "Pac4jAuthenticationScimProvisioningConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(ScimV2PrincipalAttributeMapper.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Provisioning, module = "pac4j")
    public static class Pac4jAuthenticationScimProvisioningConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pac4j.provisioning.scim.enabled").isTrue();
        @Bean
        @ConditionalOnMissingBean(name = "pac4jScimDelegatedClientUserProfileProvisioner")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<DelegatedClientUserProfileProvisioner> pac4jScimDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PrincipalProvisioner.BEAN_NAME)
            final PrincipalProvisioner principalProvisioner) {
            return BeanSupplier.of(Supplier.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> () -> new ScimDelegatedClientUserProfileProvisioner(principalProvisioner))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "groovyDelegatedClientUserProfileProvisioner")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<DelegatedClientUserProfileProvisioner> groovyDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.pac4j.provisioning.groovy.location").exists()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
                    val script = provisioning.getGroovy().getLocation();
                    return () -> new GroovyDelegatedClientUserProfileProvisioner(script);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restDelegatedClientUserProfileProvisioner")
        public Supplier<DelegatedClientUserProfileProvisioner> restDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) throws Exception {
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.pac4j.provisioning.rest.url").isUrl().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
                    return () -> new RestfulDelegatedClientUserProfileProvisioner(provisioning.getRest());
                })
                .otherwiseProxy()
                .get();
        }
    }
}
