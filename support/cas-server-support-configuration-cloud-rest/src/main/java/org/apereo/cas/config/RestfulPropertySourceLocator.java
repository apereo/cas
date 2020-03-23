package org.apereo.cas.config;

import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
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

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

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

            val headers = getHttpHeaders(environment);
            val method = StringUtils.defaultIfBlank(getPropertyFromEnvironment(environment, "method"), HttpMethod.GET.name());
            response = HttpUtils.execute(url, method, basicAuthUsername, basicAuthPassword, headers);
            if (response != null && response.getEntity() != null) {
                val results = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.trace("Received response from endpoint [{}} as [{}]", url, results);
                val payload = MAPPER.readValue(results, Map.class);
                props.putAll(payload);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }

        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }

    /**
     * Gets http headers.
     *
     * @param environment the environment
     * @return the http headers
     */
    protected HashMap<String, Object> getHttpHeaders(final Environment environment) {
        val headersPassed = getPropertyFromEnvironment(environment, "headers");

        val headers = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(headersPassed)) {
            Arrays.stream(headersPassed.split(";")).forEach(headerAndValue -> {
                val values = Splitter.on(":").splitToList(headerAndValue);
                if (values.size() == 2) {
                    headers.put(values.get(0), values.get(1));
                }
            });
        }
        return headers;
    }
}
