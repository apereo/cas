package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.core.authentication.passwordsync.RestfulPasswordSynchronizationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is {@link RestfulPasswordSynchronizationAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulPasswordSynchronizationAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    
    private final RestfulPasswordSynchronizationProperties properties;

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        if (properties.isAsynchronous()) {
            executorService.execute(() -> synchronize(transaction));
        } else {
            synchronize(transaction);
        }
    }

    private void synchronize(final AuthenticationTransaction transaction) {
        HttpResponse response = null;
        try {
            val primaryCredential = (UsernamePasswordCredential) transaction.getPrimaryCredential().get();
            val entity = MAPPER.writeValueAsString(Map.of("username", primaryCredential.getId(), "password", primaryCredential.toPassword()));
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .url(properties.getUrl())
                .basicAuthPassword(properties.getBasicAuthUsername())
                .basicAuthUsername(properties.getBasicAuthPassword())
                .method(HttpMethod.POST)
                .entity(entity)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public boolean supports(final Credential credential) throws Throwable {
        return credential instanceof UsernamePasswordCredential;
    }
}
