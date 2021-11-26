package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link OidcRestfulJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcRestfulJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService {
    private final OidcProperties oidcProperties;

    @Override
    public Optional<Resource> find() throws Exception {
        return Optional.ofNullable(generate());
    }

    @Override
    public Resource generate() throws Exception {
        val rest = oidcProperties.getJwks().getRest();
        val exec = HttpUtils.HttpExecutionRequest.builder()
            .basicAuthPassword(rest.getBasicAuthPassword())
            .basicAuthUsername(rest.getBasicAuthUsername())
            .method(HttpMethod.GET)
            .url(rest.getUrl())
            .build();
        val response = HttpUtils.execute(exec);
        if (response == null || !HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
            LOGGER.warn("Unable to successfully fetch JWKS resource from [{}]", rest.getUrl());
            return null;
        }

        val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        LOGGER.debug("Received payload result from [{}] as [{}]", rest.getUrl(), result);
        return new ByteArrayResource(result.getBytes(StandardCharsets.UTF_8), "OIDC JWKS");
    }

    @Override
    public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) throws Exception {
        val rest = oidcProperties.getJwks().getRest();
        val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.putAll(rest.getHeaders());
        val exec = HttpUtils.HttpExecutionRequest.builder()
            .basicAuthPassword(rest.getBasicAuthPassword())
            .basicAuthUsername(rest.getBasicAuthUsername())
            .method(HttpMethod.POST)
            .url(rest.getUrl())
            .headers(headers)
            .entity(jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE))
            .build();
        val response = HttpUtils.execute(exec);
        FunctionUtils.doIfNotNull(response,
            httpResponse -> LOGGER.debug("Storing JWKS resource via [{}] returned [{}]",
                rest.getUrl(), response.getStatusLine()));
        return jsonWebKeySet;
    }
}
