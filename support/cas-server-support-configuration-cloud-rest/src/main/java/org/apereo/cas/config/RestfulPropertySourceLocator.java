package org.apereo.cas.config;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
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

            val basicAuthUsername = getPropertyFromEnvironment(environment, "basic-auth-username");
            val basicAuthPassword = getPropertyFromEnvironment(environment, "basic-auth-password");

            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            val method = StringUtils.defaultIfBlank(getPropertyFromEnvironment(environment, "method"), HttpMethod.GET.name());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(basicAuthPassword)
                .basicAuthUsername(basicAuthUsername)
                .method(HttpMethod.valueOf(method.toUpperCase(Locale.ENGLISH)))
                .url(url)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && ((HttpEntityContainer) response).getEntity() != null) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                    LOGGER.trace("Received response from endpoint [{}] as [{}]", url, results);
                    val payload = MAPPER.readValue(results, Map.class);
                    props.putAll(payload);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }

        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
