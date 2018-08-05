package org.springframework.boot.configurationmetadata;

import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasConfigurationMetadataRepositoryJsonBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public final class CasConfigurationMetadataRepositoryJsonBuilder {
    private Charset defaultCharset = StandardCharsets.UTF_8;

    private final JsonReader reader = new JsonReader();

    private final List<SimpleConfigurationMetadataRepository> repositories = new ArrayList<>();

    private CasConfigurationMetadataRepositoryJsonBuilder(final Charset defaultCharset) {
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
     * @throws IOException in case of I/O errors
     */
    public CasConfigurationMetadataRepositoryJsonBuilder withJsonResource(final InputStream inputStream) throws IOException {
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
     * @throws IOException in case of I/O errors
     */
    public CasConfigurationMetadataRepositoryJsonBuilder withJsonResource(final InputStream inputStream, final Charset charset) throws IOException {
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
        SimpleConfigurationMetadataRepository result = new SimpleConfigurationMetadataRepository();
        for (SimpleConfigurationMetadataRepository repository : this.repositories) {
            result.include(repository);
        }
        return result;
    }

    private SimpleConfigurationMetadataRepository add(final InputStream in, final Charset charset) {
        try {
            RawConfigurationMetadata metadata = this.reader.read(in, charset);
            return create(metadata);
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to read configuration metadata", ex);
        }
    }

    private SimpleConfigurationMetadataRepository create(final RawConfigurationMetadata metadata) {
        SimpleConfigurationMetadataRepository repository = new SimpleConfigurationMetadataRepository();
        repository.add(metadata.getSources());
        for (val item : metadata.getItems()) {
            ConfigurationMetadataSource source = getSource(metadata, item);
            repository.add(item, source);
        }
        Map<String, ConfigurationMetadataProperty> allProperties = repository.getAllProperties();
        for (final ConfigurationMetadataHint hint : metadata.getHints()) {
            ConfigurationMetadataProperty property = allProperties.get(hint.getId());
            if (property != null) {
                addValueHints(property, hint);
            } else {
                String id = hint.resolveId();
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

    private void addValueHints(final ConfigurationMetadataProperty property, final ConfigurationMetadataHint hint) {
        property.getHints().getValueHints().addAll(hint.getValueHints());
        property.getHints().getValueProviders().addAll(hint.getValueProviders());
    }

    private void addMapHints(final ConfigurationMetadataProperty property, final ConfigurationMetadataHint hint) {
        property.getHints().getKeyHints().addAll(hint.getValueHints());
        property.getHints().getKeyProviders().addAll(hint.getValueProviders());
    }

    private ConfigurationMetadataSource getSource(final RawConfigurationMetadata metadata, final ConfigurationMetadataItem item) {
        if (item.getSourceType() == null) {
            return null;
        }
        val name = item.getId().substring(0, item.getId().lastIndexOf('.'));
        for (ConfigurationMetadataSource source : metadata.getSources()) {
            if (source.getType().equals(item.getSourceType()) && name.equals(source.getGroupId())) {
                return source;
            }
        }
        return null;
    }

    /**
     * Create a new builder instance using {@link StandardCharsets#UTF_8} as the default
     * charset and the specified json resource.
     *
     * @param inputStreams the source input streams
     * @return a new {@link ConfigurationMetadataRepositoryJsonBuilder} instance.
     * @throws IOException on error
     */
    public static CasConfigurationMetadataRepositoryJsonBuilder create(final InputStream... inputStreams) throws IOException {
        CasConfigurationMetadataRepositoryJsonBuilder builder = create();
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
