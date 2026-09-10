package org.apereo.cas.metadata;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.base.Suppliers;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.CasConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * This is {@link CasConfigurationMetadataRepository}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
public class CasConfigurationMetadataRepository {
    /**
     * Bean name for the CAS configuration metadata repository.
     */
    public static final String BEAN_NAME = "casConfigurationMetadataRepository";

    private static final String CONFIGURATION_METADATA_RESOURCE_PATTERN = "classpath*:META-INF/spring-configuration-metadata.json";

    private final Supplier<ConfigurationMetadataRepository> repository;

    public CasConfigurationMetadataRepository() {
        this(CasConfigurationMetadataRepository::collectConfigurationMetadata);
    }

    public CasConfigurationMetadataRepository(final List<byte[]> sources) {
        this(() -> sources);
    }

    private CasConfigurationMetadataRepository(final Supplier<List<byte[]>> sources) {
        this.repository = Suppliers.memoize(() -> buildRepository(sources.get()));
    }

    /**
     * Gets the underlying configuration metadata repository.
     * Collecting and parsing every {@code spring-configuration-metadata.json} on the classpath is
     * expensive, so the index is built on first access rather than when this instance is created.
     *
     * @return the repository
     */
    public ConfigurationMetadataRepository getRepository() {
        return repository.get();
    }

    private static ConfigurationMetadataRepository buildRepository(final List<byte[]> sources) {
        val builder = CasConfigurationMetadataRepositoryJsonBuilder.create();
        sources.forEach(Unchecked.consumer(buffer -> {
            try (val stream = new ByteArrayInputStream(buffer)) {
                builder.withJsonResource(stream, StandardCharsets.UTF_8);
            }
        }));
        return builder.build();
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
        return getRepository().getAllProperties().values()
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
