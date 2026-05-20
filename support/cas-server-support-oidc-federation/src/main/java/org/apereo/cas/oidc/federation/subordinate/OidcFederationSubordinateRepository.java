package org.apereo.cas.oidc.federation.subordinate;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcFederationSubordinateRepository}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Slf4j
public class OidcFederationSubordinateRepository {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Getter
    private Map<String, OidcFederationSubordinate> subordinates = new HashMap<>();

    public OidcFederationSubordinateRepository(final OidcProperties oidcProperties) {
        loadSubordinates(oidcProperties.getFederation().getSubordinateDirectory());
    }

    protected void loadSubordinates(final String subordinateDirectory) {
        if (StringUtils.isNotBlank(subordinateDirectory)) {
            LOGGER.debug("Loading subordinates...");
            val dir = Paths.get(subordinateDirectory);
            if (!Files.exists(dir)) {
                throw new IllegalArgumentException("subordinate directory [" + subordinateDirectory + "] does not exist");
            }
            if (!Files.isDirectory(dir)) {
                throw new IllegalArgumentException("subordinate directory [" + subordinateDirectory + "] is not a directory");
            }
            try (val stream = Files.walk(dir)) {
                stream.filter(Files::isRegularFile).forEach(f -> {
                    val file = f.toFile();
                    LOGGER.debug("Parsing [{}]...", f.toString());
                    val subordinate = MAPPER.readValue(file, OidcFederationSubordinate.class);
                    subordinates.put(subordinate.getEntityId(), subordinate);
                });
            } catch (final IOException e) {
                throw new IllegalArgumentException("Cannot read/load from subordinate directory", e);
            }
            LOGGER.info("Loaded [{}] subordinates", subordinates.size());
        }
    }
}
