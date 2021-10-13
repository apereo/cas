package org.apereo.cas.config.pm;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.rest.RestPasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.client.RestTemplate;

/**
 * This is {@link RestPasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "restPasswordManagementConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class RestPasswordManagementConfiguration {

    private static RestTemplate buildRestTemplateBuilder(final RestTemplateBuilder restTemplateBuilder,
                                                         final CasConfigurationProperties casProperties) {
        val pmRest = casProperties.getAuthn().getPm().getRest();
        val username = pmRest.getEndpointUsername();
        val password = pmRest.getEndpointPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            LOGGER.debug("Configuring basic authentication for password management via REST for [{}]", username);
            return restTemplateBuilder.basicAuthentication(username, password).build();
        }
        LOGGER.warn("Basic authentication for password management via REST is turned off");
        return restTemplateBuilder.build();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public PasswordManagementService passwordChangeService(final RestTemplateBuilder restTemplateBuilder,
                                                           final CasConfigurationProperties casProperties,
                                                           @Qualifier("passwordManagementCipherExecutor")
                                                           final CipherExecutor passwordManagementCipherExecutor,
                                                           @Qualifier("passwordHistoryService")
                                                           final PasswordHistoryService passwordHistoryService) {
        var pm = casProperties.getAuthn().getPm();
        return new RestPasswordManagementService(passwordManagementCipherExecutor,
            casProperties.getServer().getPrefix(),
            buildRestTemplateBuilder(restTemplateBuilder, casProperties), pm,
            passwordHistoryService);
    }
}
