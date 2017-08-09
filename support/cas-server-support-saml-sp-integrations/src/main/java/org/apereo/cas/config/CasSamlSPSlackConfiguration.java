package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSamlSPSlackConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casSamlSPSlackConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPSlackConfiguration extends BaseCasSamlSPConfiguration {

    @Override
    protected AbstractSamlSPProperties getServiceProvider() {
        return casProperties.getSamlSp().getSlack();
    }
}
