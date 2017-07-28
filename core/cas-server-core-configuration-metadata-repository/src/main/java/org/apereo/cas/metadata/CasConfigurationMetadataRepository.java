package org.apereo.cas.metadata;

import com.google.common.base.Throwables;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.Arrays;

/**
 * This is {@link CasConfigurationMetadataRepository}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
public class CasConfigurationMetadataRepository {
    private final ConfigurationMetadataRepository configMetadataRepo;

    /**
     * Instantiates a new Cas configuration metadata repository.
     * Scans the context looking for spring configuration metadata
     * resources and then loads them all into a repository instance.
     */
    public CasConfigurationMetadataRepository() {
        try {
            final Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:META-INF/spring-configuration-metadata.json");
            final ConfigurationMetadataRepositoryJsonBuilder builder = ConfigurationMetadataRepositoryJsonBuilder.create();
            Arrays.stream(resources).forEach(Unchecked.consumer(r -> {
                try (InputStream in = r.getInputStream()) {
                    builder.withJsonResource(in);
                }
            }));
            configMetadataRepo = builder.build();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public ConfigurationMetadataRepository getRepository() {
        return configMetadataRepo;
    }
}
