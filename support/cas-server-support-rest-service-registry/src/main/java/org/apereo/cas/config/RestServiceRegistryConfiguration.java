package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.services.RestServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link RestServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("restServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestServiceRegistryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceRegistryConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public ServiceRegistryDao serviceRegistryDao() {
        try {
            final ServiceRegistryProperties registry = casProperties.getServiceRegistry();
            final RestTemplate restTemplate = new RestTemplate();
            final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

            if (StringUtils.isNotBlank(registry.getRest().getBasicAuthUsername())
                    && StringUtils.isNotBlank(registry.getRest().getBasicAuthPassword())) {
                final String auth = registry.getRest().getBasicAuthUsername() + ":" + registry.getRest().getBasicAuthPassword();
                final byte[] encodedAuth = EncodingUtils.encodeBase64ToByteArray(auth.getBytes(StandardCharsets.UTF_8));
                final String authHeader = "Basic " + new String(encodedAuth);
                headers.put("Authorization", CollectionUtils.wrap(authHeader));
            }
            return new RestServiceRegistryDao(restTemplate, registry.getRest().getUrl(), headers);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
