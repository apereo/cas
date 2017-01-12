package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.SuccessfulHandlerMetaDataPopulator;
import org.apereo.cas.authentication.principal.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCoreAuthenticationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationMetadataConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators() {
        final List<AuthenticationMetaDataPopulator> list = new ArrayList<>();
        list.add(successfulHandlerMetaDataPopulator());
        list.add(rememberMeAuthenticationMetaDataPopulator());

        if (casProperties.getClearpass().isCacheCredential()) {
            list.add(new CacheCredentialsMetaDataPopulator());
        }
        return list;
    }

    @Bean
    public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
        return new SuccessfulHandlerMetaDataPopulator();
    }

    @Bean
    public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator() {
        return new RememberMeAuthenticationMetaDataPopulator();
    }

}
