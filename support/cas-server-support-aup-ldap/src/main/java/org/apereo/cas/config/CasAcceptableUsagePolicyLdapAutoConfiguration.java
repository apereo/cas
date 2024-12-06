package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.LdapAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link CasAcceptableUsagePolicyLdapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "ldap")
@AutoConfiguration
public class CasAcceptableUsagePolicyLdapAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return BeanSupplier.of(AcceptableUsagePolicyRepository.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val connectionFactoryList = new ConcurrentHashMap<String, ConnectionFactory>();
                val aupProperties = casProperties.getAcceptableUsagePolicy();
                aupProperties.getLdap().forEach(ldap -> connectionFactoryList.put(ldap.getLdapUrl(), LdapUtils.newLdaptiveConnectionFactory(ldap)));
                return new LdapAcceptableUsagePolicyRepository(ticketRegistrySupport, aupProperties, connectionFactoryList);
            })
            .otherwise(AcceptableUsagePolicyRepository::noOp)
            .get();
    }
}
