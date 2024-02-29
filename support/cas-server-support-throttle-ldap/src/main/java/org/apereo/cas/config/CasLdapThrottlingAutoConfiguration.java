package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.LdapThrottledSubmissionReceiver;
import org.apereo.cas.web.support.ThrottledSubmissionReceiver;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasLdapThrottlingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Throttling, module = "ldap")
@AutoConfiguration
public class CasLdapThrottlingAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ldapThrottledSubmissionConnectionFactory")
    public ConnectionFactory ldapThrottledSubmissionConnectionFactory(
        final CasConfigurationProperties casProperties) {
        val ldap = casProperties.getAuthn().getThrottle().getLdap();
        return LdapUtils.newLdaptiveConnectionFactory(ldap);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ldapThrottledSubmissionReceiver")
    @Bean
    public ThrottledSubmissionReceiver ldapThrottledSubmissionReceiver(
        @Qualifier("ldapThrottledSubmissionConnectionFactory")
        final ConnectionFactory ldapThrottledSubmissionConnectionFactory,
        @Qualifier("authenticationThrottlingConfigurationContext")
        final ThrottledSubmissionHandlerConfigurationContext ctx) {
        val factory = new LdapConnectionFactory(ldapThrottledSubmissionConnectionFactory);
        return new LdapThrottledSubmissionReceiver(factory, ctx);
    }
}
