package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This is {@link OidcDefaultJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Order
public class OidcDefaultJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService, DisposableBean {

    private final OidcProperties oidcProperties;

    private final ConfigurableApplicationContext applicationContext;

    private WatcherService resourceWatcherService;

    @Override
    public void destroy() {
        FunctionUtils.doIfNotNull(resourceWatcherService, WatcherService::close);
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

    @Override
    public Resource generate() throws Exception {
        val resource = determineJsonWebKeystoreResource();
        val isWatcherEnabled = oidcProperties.getJwks().getFileSystem().isWatcherEnabled();
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (ResourceUtils.isFile(resource) && isWatcherEnabled && resourceWatcherService == null) {
            resourceWatcherService = new FileWatcherService(resource.getFile(),
                file -> new Consumer<File>() {
                    @Override
                    public void accept(final File file) {
                        FunctionUtils.doUnchecked(__ -> {
                            if (applicationContext.isActive()) {
                                LOGGER.info("Publishing event to broadcast change in [{}]", file);
                                applicationContext.publishEvent(new OidcJsonWebKeystoreModifiedEvent(this, file, clientInfo));
                            }
                        });
                    }
                });
            resourceWatcherService.start(resource.getFilename());
        }
        val resultingResource = generate(resource);
        applicationContext.publishEvent(new OidcJsonWebKeystoreGeneratedEvent(this, resultingResource, clientInfo));
        return resultingResource;
    }

    protected Resource generate(final Resource file) throws Exception {
        if (ResourceUtils.doesResourceExist(file)) {
            LOGGER.trace("Located JSON web keystore at [{}]", file);
            return file;
        }
        val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.generateJsonWebKeySet(oidcProperties);
        store(jsonWebKeySet);
        return file;
    }


    protected AbstractResource determineJsonWebKeystoreResource() throws Exception {
        val file = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(oidcProperties.getJwks().getFileSystem().getJwksFile());
        try {
            val jsonKeys = new JsonWebKeySet(file).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
            return new ByteArrayResource(jsonKeys.getBytes(StandardCharsets.UTF_8), "OpenID Connect Keystore");
        } catch (final Exception e) {
            LOGGER.debug("Given resource [{}] cannot be parsed as a raw JSON web keystore", file);
            LOGGER.trace(e.getMessage(), e);
            val resource = ResourceUtils.getRawResourceFrom(file);
            if (ResourceUtils.doesResourceExist(file)) {
                try (val is = resource.getInputStream()) {
                    val jwks = IOUtils.toString(is, StandardCharsets.UTF_8);
                    if (CasConfigurationJasyptCipherExecutor.isValueEncrypted(jwks)) {
                        val cipher = new CasConfigurationJasyptCipherExecutor(applicationContext.getEnvironment());
                        return new ByteArrayResource(cipher.decryptValue(jwks).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            return resource;
        }
    }

}
