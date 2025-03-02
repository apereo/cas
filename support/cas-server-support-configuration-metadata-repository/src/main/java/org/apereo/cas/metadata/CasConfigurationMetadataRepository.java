package org.apereo.cas.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.lucene.LuceneSearchService;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.CasConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.ByteArrayInputStream;
import java.io.File;
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
    private static final String CONFIGURATION_METADATA_RESOURCE_PATTERN = "classpath*:META-INF/spring-configuration-metadata.json";

    private final ConfigurationMetadataRepository repository;
    private final LuceneSearchService luceneSearchService = new LuceneSearchService(
        new File(FileUtils.getTempDirectory(), getClass().getSimpleName()),
        List.of("properties"), List.of("name", "description"));

    public CasConfigurationMetadataRepository() {
        this(CONFIGURATION_METADATA_RESOURCE_PATTERN);
    }

    public CasConfigurationMetadataRepository(final String resource) {
        val builder = CasConfigurationMetadataRepositoryJsonBuilder.create();
        FunctionUtils.doUnchecked(__ -> {
            val resources = new PathMatchingResourcePatternResolver().getResources(resource);
            Arrays.stream(resources).forEach(Unchecked.consumer(r -> {
                try (val in = r.getInputStream()) {
                    val data = in.readAllBytes();
                    try (val indexInputStream = new ByteArrayInputStream(data);
                         val metadataInputStream = new ByteArrayInputStream(data)) {
                        luceneSearchService.createIndexes(indexInputStream);
                        builder.withJsonResource(metadataInputStream);
                    }
                }
            }));
        });
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
}
