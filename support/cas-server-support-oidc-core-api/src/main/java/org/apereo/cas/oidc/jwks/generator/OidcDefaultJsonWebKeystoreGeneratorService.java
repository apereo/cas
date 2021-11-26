package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link OidcDefaultJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcDefaultJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService, DisposableBean {

    private final OidcProperties oidcProperties;

    private final ConfigurableApplicationContext applicationContext;

    private WatcherService resourceWatcherService;

    @Override
    public void destroy() {
        if (this.resourceWatcherService != null) {
            this.resourceWatcherService.close();
        }
    }

    @Override
    public Optional<Resource> find() throws Exception {
        val resource = determineJsonWebKeystoreResource();
        return Optional.ofNullable(ResourceUtils.doesResourceExist(resource) ? resource : null);
    }

    @Override
    public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) throws Exception {
        val resource = determineJsonWebKeystoreResource();
        if (ResourceUtils.isFile(resource)) {
            val data = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
            val location = resource.getFile();
            FileUtils.write(location, data, StandardCharsets.UTF_8);
            LOGGER.debug("Generated JSON web keystore at [{}]", location);
        }
        return jsonWebKeySet;
    }

    @SneakyThrows
    @Override
    public Resource generate() {
        val resource = determineJsonWebKeystoreResource();
        if (ResourceUtils.isFile(resource)) {
            resourceWatcherService = new FileWatcherService(resource.getFile(),
                file -> {
                    LOGGER.info("Publishing event to broadcast change in [{}]", file);
                    applicationContext.publishEvent(new OidcJsonWebKeystoreModifiedEvent(this, file));
                });
            resourceWatcherService.start(resource.getFilename());
        }
        val resultingResource = generate(resource);
        applicationContext.publishEvent(new OidcJsonWebKeystoreGeneratedEvent(this, resultingResource));
        return resultingResource;
    }

    /**
     * Generate.
     *
     * @param file the file
     * @return the resource
     */
    @SneakyThrows
    protected Resource generate(final Resource file) {
        if (ResourceUtils.doesResourceExist(file)) {
            LOGGER.trace("Located JSON web keystore at [{}]", file);
            return file;
        }
        val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.generateJsonWebKeySet(oidcProperties);
        store(jsonWebKeySet);
        return file;
    }

    private AbstractResource determineJsonWebKeystoreResource() throws Exception {
        val resolve = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(oidcProperties.getJwks().getFileSystem().getJwksFile());
        return ResourceUtils.getRawResourceFrom(resolve);
    }
}
