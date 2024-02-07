package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.DelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.authentication.principal.ldap.LdapDelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DelegatedAuthenticationProfileSelectionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@Configuration(value = "DelegatedAuthenticationProfileSelectionConfiguration", proxyBeanMethods = false)
@Slf4j
@ConditionalOnClass(ConnectionFactory.class)
class DelegatedAuthenticationProfileSelectionConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ldapDelegatedClientAuthenticationCredentialResolver")
    public DelegatedClientAuthenticationCredentialResolver ldapDelegatedClientAuthenticationCredentialResolver(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext configContext) {
        return BeanSupplier.of(DelegatedClientAuthenticationCredentialResolver.class)
            .when(BeanCondition.on("cas.authn.pac4j.profile-selection.ldap[0].ldap-url").given(applicationContext.getEnvironment()))
            .supply(() -> new LdapDelegatedClientAuthenticationCredentialResolver(configContext))
            .otherwiseProxy()
            .get();
    }
}
