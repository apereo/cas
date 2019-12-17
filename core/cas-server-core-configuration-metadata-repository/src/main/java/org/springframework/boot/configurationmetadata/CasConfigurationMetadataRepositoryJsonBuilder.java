package org.springframework.boot.configurationmetadata;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link CasConfigurationMetadataRepositoryJsonBuilder}
 * which is similar to {@link ConfigurationMetadataRepositoryJsonBuilder}
 * with a different implementation for the {@link #getSource(RawConfigurationMetadata, ConfigurationMetadataItem)}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CasConfigurationMetadataRepositoryJsonBuilder {
    private Charset defaultCharset = StandardCharsets.UTF_8;

    private final JsonReader reader = new JsonReader();

    private final List<SimpleConfigurationMetadataRepository> repositories = new ArrayList<>(0);

    CasConfigurationMetadataRepositoryJsonBuilder(final Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * Add the content of a {@link ConfigurationMetadataRepository} defined by the
     * specified {@link InputStream} json document using the default charset. If this
     * metadata repository holds items that were loaded previously, these are ignored.
     * <p>
     * Leaves the stream open when done.
     *
     * @param inputStream the source input stream
     * @return this builder
     */
    public CasConfigurationMetadataRepositoryJsonBuilder withJsonResource(final InputStream inputStream) {
        return withJsonResource(inputStream, this.defaultCharset);
    }

    /**
     * Add the content of a {@link ConfigurationMetadataRepository} defined by the
     * specified {@link InputStream} json document using the specified {@link Charset}. If
     * this metadata repository holds items that were loaded previously, these are
     * ignored.
     * <p>
     * Leaves the stream open when done.
     *
     * @param inputStream the source input stream
     * @param charset     the charset of the input
     * @return this builder
     */
    public CasConfigurationMetadataRepositoryJsonBuilder withJsonResource(final InputStream inputStream, final Charset charset) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream must not be null.");
        }
        this.repositories.add(add(inputStream, charset));
        return this;
    }

    /**
     * Build a {@link ConfigurationMetadataRepository} with the current state of this
     * builder.
     *
     * @return this builder
     */
    public ConfigurationMetadataRepository build() {
        val result = new SimpleConfigurationMetadataRepository();
        for (val repository : this.repositories) {
            result.include(repository);
        }
        return result;
    }

    private SimpleConfigurationMetadataRepository add(final InputStream in, final Charset charset) {
        try {
            val metadata = this.reader.read(in, charset);
            return create(metadata);
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to read configuration metadata", ex);
        }
    }

    private static void addValueHints(final ConfigurationMetadataProperty property, final ConfigurationMetadataHint hint) {
        property.getHints().getValueHints().addAll(hint.getValueHints());
        property.getHints().getValueProviders().addAll(hint.getValueProviders());
    }

    private static void addMapHints(final ConfigurationMetadataProperty property, final ConfigurationMetadataHint hint) {
        property.getHints().getKeyHints().addAll(hint.getValueHints());
        property.getHints().getKeyProviders().addAll(hint.getValueProviders());
    }

    private static ConfigurationMetadataSource getSource(final RawConfigurationMetadata metadata, final ConfigurationMetadataItem item) {
        if (item.getSourceType() == null) {
            return null;
        }

        val idx = item.getId().lastIndexOf('.');
        val name = idx > 0 ? item.getId().substring(0, idx) : StringUtils.EMPTY;

        return metadata.getSources().stream()
            .filter(source -> Objects.equals(source.getType(), item.getSourceType()) && name.equals(source.getGroupId()))
            .findFirst()
            .orElse(null);

    }

    private static SimpleConfigurationMetadataRepository create(final RawConfigurationMetadata metadata) {
        val repository = new SimpleConfigurationMetadataRepository();
        repository.add(metadata.getSources());
        for (val item : metadata.getItems()) {
            val source = getSource(metadata, item);
            repository.add(item, source);
        }
        val allProperties = repository.getAllProperties();
        for (val hint : metadata.getHints()) {
            var property = allProperties.get(hint.getId());
            if (property != null) {
                addValueHints(property, hint);
            } else {
                val id = hint.resolveId();
                property = allProperties.get(id);
                if (property != null) {
                    if (hint.isMapKeyHints()) {
                        addMapHints(property, hint);
                    } else {
                        addValueHints(property, hint);
                    }
                }
            }
        }
        return repository;
    }

    /**
     * Create a new builder instance using {@link StandardCharsets#UTF_8} as the default
     * charset and the specified json resource.
     *
     * @param inputStreams the source input streams
     * @return a new {@link ConfigurationMetadataRepositoryJsonBuilder} instance.
     */
    public static CasConfigurationMetadataRepositoryJsonBuilder create(final InputStream... inputStreams) {
        var builder = create();
        for (val inputStream : inputStreams) {
            builder = builder.withJsonResource(inputStream);
        }
        return builder;
    }

    /**
     * Create a new builder instance using {@link StandardCharsets#UTF_8} as the default
     * charset.
     *
     * @return a new {@link ConfigurationMetadataRepositoryJsonBuilder} instance.
     */
    public static CasConfigurationMetadataRepositoryJsonBuilder create() {
        return create(StandardCharsets.UTF_8);
    }

    /**
     * Create a new builder instance using the specified default {@link Charset}.
     *
     * @param defaultCharset the default charset to use
     * @return a new {@link ConfigurationMetadataRepositoryJsonBuilder} instance.
     */
    public static CasConfigurationMetadataRepositoryJsonBuilder create(final Charset defaultCharset) {
        return new CasConfigurationMetadataRepositoryJsonBuilder(defaultCharset);
    }
}
