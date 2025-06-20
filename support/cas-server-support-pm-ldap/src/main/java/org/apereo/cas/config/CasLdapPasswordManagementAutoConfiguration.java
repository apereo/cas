package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pm.LdapPasswordManagementService;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.impl.NoOpPasswordManagementService;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link CasLdapPasswordManagementAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagement, module = "ldap")
@AutoConfiguration
public class CasLdapPasswordManagementAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pm.ldap[0].ldap-url");

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ldapPasswordChangeService")
    public PasswordManagementService passwordChangeService(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("passwordManagementCipherExecutor")
        final CipherExecutor passwordManagementCipherExecutor,
        @Qualifier(PasswordHistoryService.BEAN_NAME)
        final PasswordHistoryService passwordHistoryService) {
        return BeanSupplier.of(PasswordManagementService.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val connectionFactoryMap = new ConcurrentHashMap<String, ConnectionFactory>();
                val passwordManagerProperties = casProperties.getAuthn().getPm();
                passwordManagerProperties.getLdap()
                    .forEach(ldap -> connectionFactoryMap.put(ldap.getLdapUrl(), LdapUtils.newLdaptiveConnectionFactory(ldap)));
                return new LdapPasswordManagementService(
                    passwordManagementCipherExecutor,
                    casProperties, passwordHistoryService,
                    connectionFactoryMap);
            })
            .otherwise(() -> new NoOpPasswordManagementService(passwordManagementCipherExecutor, casProperties))
            .get();
    }
}
