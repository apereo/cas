package org.apereo.cas.metadata;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.util.RegexUtils;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedNames;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ReflectionUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * This is {@link CasConfigurationMetadataRepository}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
public class CasConfigurationMetadataRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationMetadataRepository.class);
    private static final Pattern PATTERN_GENERICS = RegexUtils.createPattern(".+\\<(.+)\\>");

    private final ConfigurationMetadataRepository configMetadataRepo;

    public CasConfigurationMetadataRepository() {
        this("classpath*:META-INF/spring-configuration-metadata.json");
    }

    /**
     * Instantiates a new Cas configuration metadata repository.
     * Scans the context looking for spring configuration metadata
     * resources and then loads them all into a repository instance.
     *
     * @param resource the resource
     */
    public CasConfigurationMetadataRepository(final String resource) {
        try {
            final Resource[] resources = new PathMatchingResourcePatternResolver().getResources(resource);
            final ConfigurationMetadataRepositoryJsonBuilder builder = ConfigurationMetadataRepositoryJsonBuilder.create();
            Arrays.stream(resources).forEach(Unchecked.consumer(r -> {
                try (InputStream in = r.getInputStream()) {
                    builder.withJsonResource(in);
                }
            }));
            configMetadataRepo = builder.build();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public ConfigurationMetadataRepository getRepository() {
        return configMetadataRepo;
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
     * @return the boolean
     */
    public static boolean isCasProperty(final ConfigurationMetadataProperty prop) {
        return prop.getName().startsWith(CasConfigurationProperties.PREFIX.concat("."));
    }

    /**
     * Gets required property.
     *
     * @param prop the prop
     * @return the required property
     */
    public Pair<Boolean, String> getRequiredProperty(final ConfigurationMetadataProperty prop) {
        if (!isCasProperty(prop)) {
            return Pair.of(false, StringUtils.EMPTY);
        }
        final String groupId = getPropertyGroupId(prop);
        if (!getRepository().getAllGroups().containsKey(groupId)) {
            return Pair.of(false, StringUtils.EMPTY);
        }

        final ConfigurationMetadataGroup grp = getRepository().getAllGroups().get(groupId);
        final Optional<Pair<Boolean, String>> result = grp.getSources().entrySet()
                .stream()
                .map(entry -> {
                    try {
                        final Matcher matcher = PATTERN_GENERICS.matcher(entry.getKey());
                        final String className = matcher.find() ? matcher.group(1) : entry.getKey();
                        final Class clazz = ClassUtils.getClass(className);
                        final String propName = StringUtils.substringAfterLast(prop.getName(), ".");
                        return StreamSupport.stream(RelaxedNames.forCamelCase(propName).spliterator(), false)
                                .map(n -> ReflectionUtils.findField(clazz, n))
                                .filter(f -> f != null && f.isAnnotationPresent(RequiredProperty.class))
                                .findFirst()
                                .orElse(null);
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(field -> {
                    final RequiredProperty an = field.getDeclaredAnnotation(RequiredProperty.class);
                    return Pair.of(true, an.message());
                })
                .findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        return Pair.of(false, StringUtils.EMPTY);
    }

}
