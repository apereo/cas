package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.rest.RestPasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link CasRestPasswordManagementAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagement, module = "rest")
@Slf4j
@AutoConfiguration
public class CasRestPasswordManagementAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "restPasswordChangeService")
    public PasswordManagementService passwordChangeService(
        @Qualifier("passwordChangeServiceRestTemplate")
        final RestTemplate passwordChangeServiceRestTemplate,
        final CasConfigurationProperties casProperties,
        @Qualifier("passwordManagementCipherExecutor")
        final CipherExecutor passwordManagementCipherExecutor,
        @Qualifier(PasswordHistoryService.BEAN_NAME)
        final PasswordHistoryService passwordHistoryService) {
        return new RestPasswordManagementService(passwordManagementCipherExecutor,
            casProperties, passwordChangeServiceRestTemplate, passwordHistoryService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "passwordChangeServiceRestTemplate")
    public RestTemplate passwordChangeServiceRestTemplate(final CasConfigurationProperties casProperties) {
        val pmRest = casProperties.getAuthn().getPm().getRest();

        val username = pmRest.getEndpointUsername();
        val password = pmRest.getEndpointPassword();

        var builder = new RestTemplateBuilder();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            LOGGER.debug("Configuring basic authentication for password management via REST for [{}]", username);
            builder = builder.basicAuthentication(username, password, StandardCharsets.UTF_8);
        }
        builder = builder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
        for (val entry : pmRest.getHeaders().entrySet()) {
            LOGGER.debug("Configuring header [{}] with value [{}]", entry.getKey(), entry.getValue());
            builder = builder.defaultHeader(entry.getKey(), org.springframework.util.StringUtils.commaDelimitedListToStringArray(entry.getValue()));
        }

        return builder.build();
    }
}
