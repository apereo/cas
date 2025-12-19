package org.apereo.cas.oidc.jwks.generator;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * This is {@link OidcRestfulJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OidcRestfulJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService {
    private final OidcProperties oidcProperties;

    @Override
    public Optional<Resource> find() throws Exception {
        return Optional.ofNullable(generate());
    }

    @Override
    public Resource generate() throws Exception {
        val rest = oidcProperties.getJwks().getRest();
        val exec = HttpExecutionRequest.builder()
            .basicAuthPassword(rest.getBasicAuthPassword())
            .basicAuthUsername(rest.getBasicAuthUsername())
            .maximumRetryAttempts(rest.getMaximumRetryAttempts())
            .method(HttpMethod.GET)
            .headers(rest.getHeaders())
            .url(rest.getUrl())
            .build();
        val response = HttpUtils.execute(exec);
        if (response == null || !HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
            LOGGER.warn("Unable to successfully fetch JWKS resource from [{}]", rest.getUrl());
            return null;
        }

        try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
            val result = IOUtils.toString(content, StandardCharsets.UTF_8);
            LOGGER.debug("Received payload result from [{}] as [{}]", rest.getUrl(), result);
            return new ByteArrayResource(result.getBytes(StandardCharsets.UTF_8), "OIDC JWKS");
        }
    }

    @Override
    public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) {
        val rest = oidcProperties.getJwks().getRest();
        val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.putAll(rest.getHeaders());
        val exec = HttpExecutionRequest.builder()
            .basicAuthPassword(rest.getBasicAuthPassword())
            .basicAuthUsername(rest.getBasicAuthUsername())
            .maximumRetryAttempts(rest.getMaximumRetryAttempts())
            .method(HttpMethod.POST)
            .url(rest.getUrl())
            .headers(headers)
            .entity(jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE))
            .build();
        val response = HttpUtils.execute(exec);
        FunctionUtils.doIfNotNull(response,
            httpResponse -> LOGGER.debug("Storing JWKS resource via [{}] returned [{}]",
                rest.getUrl(), response.getReasonPhrase()));
        return jsonWebKeySet;
    }
}
