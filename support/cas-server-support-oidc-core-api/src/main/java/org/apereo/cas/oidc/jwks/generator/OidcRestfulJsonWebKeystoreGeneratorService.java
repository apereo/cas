package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.HttpUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

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

    @SneakyThrows
    @Override
    public Resource generate() {
        val rest = oidcProperties.getJwks().getRest();
        val response = HttpUtils.execute(rest.getUrl(), rest.getMethod(),
            rest.getBasicAuthUsername(), rest.getBasicAuthPassword());

        if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
            LOGGER.warn("Unable to successfully fetch JWKS resource from [{}]", rest.getUrl());
            return null;
        }

        val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        LOGGER.debug("Received payload result from [{}] as [{}]", rest.getUrl(), result);
        return new ByteArrayResource(result.getBytes(StandardCharsets.UTF_8), "OIDC JWKS");
    }
}
