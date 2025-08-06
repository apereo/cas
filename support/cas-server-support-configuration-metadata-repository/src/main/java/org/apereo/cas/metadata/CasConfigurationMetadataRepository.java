package org.apereo.cas.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.CasConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CasConfigurationMetadataRepository}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Getter
public class CasConfigurationMetadataRepository {
    /**
     * Bean name for the CAS configuration metadata repository.
     */
    public static final String BEAN_NAME = "casConfigurationMetadataRepository";

    private static final String CONFIGURATION_METADATA_RESOURCE_PATTERN = "classpath*:META-INF/spring-configuration-metadata.json";

    private final ConfigurationMetadataRepository repository;

    public CasConfigurationMetadataRepository() {
        this(collectConfigurationMetadata());
    }

    public CasConfigurationMetadataRepository(final List<byte[]> sources) {
        val builder = CasConfigurationMetadataRepositoryJsonBuilder.create();
        sources.forEach(Unchecked.consumer(buffer -> {
            try (val stream = new ByteArrayInputStream(buffer)) {
                builder.withJsonResource(stream, StandardCharsets.UTF_8);
            }
        }));
        repository = builder.build();
    }

    /**
     * Gets property group id.
     *
     * @param prop the prop
     * @return the property group id
     */
    public static String getPropertyGroupId(final ConfigurationMetadataProperty prop) {
        if (isCasProperty(prop)) {
            return StringUtils.substringBeforeLast(prop.getName(), ".");
        }
        return StringUtils.substringBeforeLast(prop.getId(), ".");
    }

    /**
     * Is cas property ?.
     *
     * @param prop the prop
     * @return true /false
     */
    public static boolean isCasProperty(final ConfigurationMetadataProperty prop) {
        return prop.getName().startsWith(CasConfigurationProperties.PREFIX.concat("."));
    }

    /**
     * Gets properties by class type.
     *
     * @param clazz the clazz
     * @return the properties by class type
     */
    public Set<ConfigurationMetadataProperty> getPropertiesWithType(final Class clazz) {
        return repository.getAllProperties().values()
            .stream()
            .filter(prop -> StringUtils.isNotBlank(prop.getType()))
            .filter(prop -> prop.getType().contains(clazz.getName()))
            .collect(Collectors.toSet());
    }

    private static List<byte[]> collectConfigurationMetadata() {
        return FunctionUtils.doUnchecked(() -> {
            val resources = new PathMatchingResourcePatternResolver().getResources(CONFIGURATION_METADATA_RESOURCE_PATTERN);
            return Arrays.stream(resources)
                .map(Unchecked.function(r -> {
                    try (val in = r.getInputStream()) {
                        return in.readAllBytes();
                    }
                }))
                .toList();
        });
    }
}
