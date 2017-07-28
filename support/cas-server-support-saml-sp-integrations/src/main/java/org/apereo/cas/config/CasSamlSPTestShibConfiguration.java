package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSamlSPTestShibConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSamlSPTestShibConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPTestShibConfiguration extends BaseCasSamlSPConfiguration {

    @Override
    protected AbstractSamlSPProperties getServiceProvider() {
        return casProperties.getSamlSp().getTestShib();
    }
}
