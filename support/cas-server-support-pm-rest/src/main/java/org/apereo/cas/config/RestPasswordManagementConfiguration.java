package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.RestPasswordManagementService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordManagementRestTemplate")
    public RestTemplate passwordManagementRestTemplate() {
        return new RestTemplate();
    }

    @ConditionalOnMissingBean(name = "reatPasswordChangeService")
    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService(@Qualifier("passwordManagementRestTemplate") final RestTemplate restTemplate) {
        return new RestPasswordManagementService(passwordManagementCipherExecutor.getIfAvailable(),
            casProperties.getServer().getPrefix(),
            restTemplate,
            casProperties.getAuthn().getPm());
    }
}
