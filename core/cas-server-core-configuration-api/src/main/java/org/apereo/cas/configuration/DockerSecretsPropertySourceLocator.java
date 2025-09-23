package org.apereo.cas.configuration;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DockerSecretsPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class DockerSecretsPropertySourceLocator implements CasConfigurationPropertiesSourceLocator {
    /**
     * The default secrets directory.
     */
    public static final String DEFAULT_SECRETS_DIR = "/run/secrets/";
    /**
     * The system property or environment variable that indicates the location of the Docker secrets directory.
     */
    public static final String VAR_CAS_DOCKER_SECRETS_DIRECTORY = "CAS_DOCKER_SECRETS_DIRECTORY";
    /**
     * The environment/system variable that indicates the location of the Docker secrets directory.
     */
    public static final String VAR_CONTAINER = "CONTAINER";

    private static final String SECRETS_DIR =
        StringUtils.defaultIfBlank(System.getProperty(VAR_CAS_DOCKER_SECRETS_DIRECTORY, System.getenv(DEFAULT_SECRETS_DIR)), DEFAULT_SECRETS_DIR);

    /**
     * Loads all Docker secrets from the secrets directory and stores them in a Map.
     *
     * @return a Map of secret names and their values.
     */
    protected Map<String, Object> loadSecrets() {
        return FunctionUtils.doUnchecked(() -> {
            val secrets = new HashMap<String, Object>();
            val secretsDir = Paths.get(SECRETS_DIR);
            LOGGER.debug("Looking for Docker secrets in [{}]", secretsDir);
            if (Files.exists(secretsDir) && Files.isDirectory(secretsDir)) {
                try (val stream = Files.newDirectoryStream(secretsDir)) {
                    for (val entry : stream) {
                        val secretName = entry.getFileName().toString();
                        val secretValue = Files.readString(entry);
                        LOGGER.debug("Found Docker secret [{}] from [{}]", secretName, entry.getFileName());
                        secrets.put(secretName, secretValue);
                    }
                }
            }
            return secrets;
        });
    }

    @Override
    public Optional<PropertySource<?>> locate(final Environment environment, final ResourceLoader resourceLoader) {
        val container = System.getProperty(VAR_CONTAINER, System.getenv(VAR_CONTAINER));
        val properties = StringUtils.isNotBlank(container) && BooleanUtils.toBoolean(container) ? loadSecrets() : Map.<String, Object>of();
        LOGGER.debug("Docker secrets loaded are [{}]", properties.keySet());
        return Optional.of(new MapPropertySource(getClass().getSimpleName(), properties));
    }
}
