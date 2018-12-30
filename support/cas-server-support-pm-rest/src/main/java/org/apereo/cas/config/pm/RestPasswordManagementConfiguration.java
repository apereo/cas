package org.apereo.cas.config.pm;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.rest.RestPasswordManagementService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


/**
 * This is {@link RestPasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("restPasswordManagementConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestPasswordManagementConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private ObjectProvider<CipherExecutor> passwordManagementCipherExecutor;

    @RefreshScope
    @Bean
    @Autowired
    public PasswordManagementService passwordChangeService(final RestTemplateBuilder restTemplateBuilder) {
        var pm = casProperties.getAuthn().getPm();
        return new RestPasswordManagementService(passwordManagementCipherExecutor.getIfAvailable(),
            casProperties.getServer().getPrefix(),
            buildRestTemplateBuilder(restTemplateBuilder),
            pm);
    }

    private RestTemplate buildRestTemplateBuilder(final RestTemplateBuilder restTemplateBuilder) {
        final PasswordManagementProperties.Rest pmRest = casProperties.getAuthn().getPm().getRest();
        var username = pmRest.getEndpointUsername();
        var password = pmRest.getEndpointPassword();

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            return restTemplateBuilder.basicAuthentication(username, password).build();
        }
        return restTemplateBuilder.build();
    }
}
