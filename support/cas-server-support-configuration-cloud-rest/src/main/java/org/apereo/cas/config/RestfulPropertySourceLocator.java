package org.apereo.cas.config;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link RestfulPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class RestfulPropertySourceLocator implements PropertySourceLocator {
    /**
     * Configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.rest";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Gets property.
     *
     * @param environment the environment
     * @param key         the key
     * @return the property
     */
    protected String getPropertyFromEnvironment(final Environment environment, final String key) {
        return environment.getProperty(CAS_CONFIGURATION_PREFIX + '.' + key);
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();

        HttpResponse response = null;
        try {
            val url = getPropertyFromEnvironment(environment, "url");
            if (StringUtils.isBlank(url)) {
                LOGGER.debug("No URL endpoint is defined to fetch CAS settings");
                return new PropertiesPropertySource(getClass().getSimpleName(), props);
            }

            val basicAuthUsername = getPropertyFromEnvironment(environment, "basicAuthUsername");
            val basicAuthPassword = getPropertyFromEnvironment(environment, "basicAuthPassword");

            val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            val method = StringUtils.defaultIfBlank(getPropertyFromEnvironment(environment, "method"), HttpMethod.GET.name());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(basicAuthPassword)
                .basicAuthUsername(basicAuthUsername)
                .method(HttpMethod.valueOf(method.toUpperCase()))
                .url(url)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && response.getEntity() != null) {
                val results = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.trace("Received response from endpoint [{}] as [{}]", url, results);
                val payload = MAPPER.readValue(results, Map.class);
                props.putAll(payload);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }

        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
