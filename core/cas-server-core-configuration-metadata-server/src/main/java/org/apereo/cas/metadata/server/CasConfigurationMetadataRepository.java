package org.apereo.cas.metadata.server;

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
 * @since 5.2.0
 */
public class CasConfigurationMetadataRepository {
    private final ConfigurationMetadataRepository configMetadataRepo;

    public CasConfigurationMetadataRepository() throws Exception {
        final Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:META-INF/spring-configuration-metadata.json");
        final ConfigurationMetadataRepositoryJsonBuilder builder = ConfigurationMetadataRepositoryJsonBuilder.create();
        Arrays.stream(resources).forEach(Unchecked.consumer(r -> {
            try (InputStream in = r.getInputStream()) {
                builder.withJsonResource(in);
            }
        }));
        configMetadataRepo = builder.build();
    }

    public ConfigurationMetadataRepository getRepository() {
        return configMetadataRepo;
    }
}
