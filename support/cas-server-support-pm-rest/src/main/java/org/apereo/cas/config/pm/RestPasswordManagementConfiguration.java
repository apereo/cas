package org.apereo.cas.config.pm;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.rest.RestPasswordManagementService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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
    public PasswordManagementService passwordChangeService(RestTemplateBuilder restTemplateBuilder) {
        PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        String username = pm.getRest().getEndpointUsername();
        String password = pm.getRest().getEndpointPassword();

        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            restTemplateBuilder = restTemplateBuilder
                    .basicAuthentication(username, password);
        }

        return new RestPasswordManagementService(passwordManagementCipherExecutor.getIfAvailable(),
                casProperties.getServer().getPrefix(),
                restTemplateBuilder.build(),
                pm);
    }
}
