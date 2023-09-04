package org.apereo.cas.adaptors.authy.config.support.authentication;

import org.apereo.cas.adaptors.authy.config.OktaMfaProperties;
import org.apereo.cas.adaptors.authy.core.AuthyTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link AuthyAuthenticationComponentSerializationConfiguration}.
 *
 * @author Jérémie POISSON
 * @since 6.0.0
 */
@EnableConfigurationProperties({CasConfigurationProperties.class, OktaMfaProperties.class})
//@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authy)
@AutoConfiguration
public class AuthyAuthenticationComponentSerializationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authyComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer authyComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(AuthyTokenCredential.class);
    }
}
