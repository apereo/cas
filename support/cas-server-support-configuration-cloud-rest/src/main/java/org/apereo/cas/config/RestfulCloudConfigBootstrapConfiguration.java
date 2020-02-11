package org.apereo.cas.config;

import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
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
 * This is {@link RestfulCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration("restfulCloudConfigBootstrapConfiguration")
@Slf4j
public class RestfulCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    /**
     * Configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.rest";

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static String getProperty(final Environment environment, final String key) {
        return environment.getProperty(CAS_CONFIGURATION_PREFIX + '.' + key);
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();

        HttpResponse response = null;
        try {
            val url = getProperty(environment, "url");
            val basicAuthUsername = getProperty(environment, "basicAuthUsername");
            val basicAuthPassword = getProperty(environment, "basicAuthPassword");
            val headersPassed = getProperty(environment, "headers");

            val headers = new HashMap<String, Object>();
            if (StringUtils.isNotBlank(headersPassed)) {
                Arrays.stream(headersPassed.split(";")).forEach(headerAndValue -> {
                    val pair = headerAndValue.split(":");
                    headers.put(pair[0], pair[1]);
                });
            }
            val method = StringUtils.defaultIfBlank(getProperty(environment, "method"), HttpMethod.GET.name());
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
}
