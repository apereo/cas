package org.apereo.cas.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.CasConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Arrays;

/**
 * This is {@link CasConfigurationMetadataRepository}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Getter
public class CasConfigurationMetadataRepository {
    private final ConfigurationMetadataRepository repository;

    public CasConfigurationMetadataRepository() {
        this("classpath*:META-INF/spring-configuration-metadata.json");
    }

    /**
     * Instantiates a new CAS configuration metadata repository.
     * Scans the context looking for spring configuration metadata
     * resources and then loads them all into a repository instance.
     *
     * @param resource the resource
     */
    @SneakyThrows
    public CasConfigurationMetadataRepository(final String resource) {
        val resources = new PathMatchingResourcePatternResolver().getResources(resource);
        val builder = CasConfigurationMetadataRepositoryJsonBuilder.create();
        Arrays.stream(resources).forEach(Unchecked.consumer(r -> {
            try (val in = r.getInputStream()) {
                builder.withJsonResource(in);
            }
        }));
        repository = builder.build();
    }

    @SneakyThrows
    public CasConfigurationMetadataRepository(final Resource resource) {
        val builder = CasConfigurationMetadataRepositoryJsonBuilder.create();
        try (val in = resource.getInputStream()) {
            builder.withJsonResource(in);
        }
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
     * @return true/false
     */
    public static boolean isCasProperty(final ConfigurationMetadataProperty prop) {
        return prop.getName().startsWith(CasConfigurationProperties.PREFIX.concat("."));
    }
}
