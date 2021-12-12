package org.apereo.cas.config;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.GroovyDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.RestfulDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.ScimDelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@Configuration(value = "Pac4jAuthenticationProvisioningConfiguration", proxyBeanMethods = false)
public class Pac4jAuthenticationProvisioningConfiguration {

    @Configuration(value = "Pac4jAuthenticationScimProvisioningConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(PrincipalProvisioner.class)
    @ConditionalOnProperty(prefix = "cas.authn.pac4j.provisioning.scim", name = "enabled", havingValue = "true")
    public static class Pac4jAuthenticationScimProvisioningConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "pac4jScimDelegatedClientUserProfileProvisioner")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<DelegatedClientUserProfileProvisioner> pac4jScimDelegatedClientUserProfileProvisioner(
            @Qualifier(PrincipalProvisioner.BEAN_NAME)
            final PrincipalProvisioner scimProvisioner) {
            return () -> new ScimDelegatedClientUserProfileProvisioner(scimProvisioner);
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration {
        @Bean
        @ConditionalOnProperty(name = "cas.authn.pac4j.provisioning.groovy.location")
        @ConditionalOnMissingBean(name = "groovyDelegatedClientUserProfileProvisioner")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<DelegatedClientUserProfileProvisioner> groovyDelegatedClientUserProfileProvisioner(
            final CasConfigurationProperties casProperties) {
            val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
            val script = provisioning.getGroovy().getLocation();
            return () -> new GroovyDelegatedClientUserProfileProvisioner(script);
        }

        @Bean
        @ConditionalOnProperty(name = "cas.authn.pac4j.provisioning.rest.url")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restDelegatedClientUserProfileProvisioner")
        public Supplier<DelegatedClientUserProfileProvisioner> restDelegatedClientUserProfileProvisioner(
            final CasConfigurationProperties casProperties) {
            val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
            return () -> new RestfulDelegatedClientUserProfileProvisioner(provisioning.getRest());
        }
    }
}
