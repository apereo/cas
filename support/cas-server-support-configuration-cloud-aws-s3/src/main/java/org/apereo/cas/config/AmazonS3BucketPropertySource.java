package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.InputStreamResource;
import org.yaml.snakeyaml.Yaml;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * This is {@link AmazonS3BucketPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@SuppressWarnings("NullAway.Init")
public class AmazonS3BucketPropertySource extends EnumerablePropertySource<Supplier<S3Client>> implements MutablePropertySource<Supplier<S3Client>> {
    private final Map<String, S3Property> properties = new ConcurrentHashMap<>();

    private final String bucketName;

    public AmazonS3BucketPropertySource(final String name, final Supplier<S3Client> s3ClientFactory, final String bucketName) {
        super(name, s3ClientFactory);
        this.bucketName = bucketName;
        refresh();
    }

    @Override
    public void refresh() {
        withClient(client -> refresh(client));
    }

    private void refresh(final S3Client client) {
        val result = client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        val objects = result.contents();
        objects.forEach(obj -> {
            val objectKey = obj.key();
            LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);
            try (val is = client.getObject(GetObjectRequest.builder().bucket(bucketName).key(objectKey).build())) {
                if (objectKey.endsWith("properties")) {
                    val props = new Properties();
                    props.load(is);
                    props.forEach((key, value) -> properties.put(key.toString(), new S3Property(objectKey, value)));
                } else if (isYaml(objectKey)) {
                    val yamlProps = CasCoreConfigurationUtils.loadYamlProperties(new InputStreamResource(is));
                    yamlProps.forEach((key, value) -> properties.put(key, new S3Property(objectKey, value)));
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        });
    }

    @Override
    public void removeAll() {
        properties.keySet().forEach(this::removeProperty);
        properties.clear();
    }

    @Override
    public void removeProperty(final String name) {
        if (properties.containsKey(name)) {
            val objectKey = properties.get(name).source();
            properties.remove(name);
            store(objectKey);
        }
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        return properties.containsKey(name) ? properties.get(name).value() : null;
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        val configSource = properties.containsKey(name) ? properties.get(name).source() : "cas.properties";
        if (isYaml(configSource) || configSource.endsWith("properties")) {
            properties.put(name, new S3Property(configSource, value));
            store(configSource);
        }
        return this;
    }

    /**
     * Writes back the whole object that holds the given settings, with every setting it still carries,
     * so that changing or removing one setting leaves the others in the object intact. An object
     * left with no settings is deleted from the bucket.
     *
     * @param objectKey the key of the object in the bucket
     */
    private void store(final String objectKey) {
        withClient(client -> store(client, objectKey));
    }

    private void store(final S3Client client, final String objectKey) {
        val entries = properties.entrySet()
            .stream()
            .filter(entry -> entry.getValue().source().equals(objectKey))
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().value(), (first, second) -> second, TreeMap::new));
        if (entries.isEmpty()) {
            client.deleteObject(builder -> builder.bucket(bucketName).key(objectKey));
            return;
        }
        val request = PutObjectRequest.builder().key(objectKey).bucket(bucketName).build();
        if (isYaml(objectKey)) {
            val yamlMap = CasCoreConfigurationUtils.convertToNestedMap(entries);
            client.putObject(request, RequestBody.fromString(new Yaml().dump(yamlMap)));
        } else {
            val props = new Properties();
            entries.forEach((key, value) -> props.setProperty(key, String.valueOf(value)));
            val writer = new StringWriter();
            FunctionUtils.doUnchecked(_ -> props.store(writer, null));
            client.putObject(request, RequestBody.fromString(writer.toString()));
        }
    }

    /**
     * Runs the operation with a client of its own and closes the client afterwards. The bootstrap context that locates
     * this property source is closed once a refresh is done, so a client shared with it would be closed while the
     * property source is still in use.
     *
     * @param operation the operation
     */
    private void withClient(final Consumer<S3Client> operation) {
        try (val client = getSource().get()) {
            operation.accept(client);
        }
    }

    private static boolean isYaml(final String objectKey) {
        return objectKey.endsWith("yml") || objectKey.endsWith("yaml");
    }

    private record S3Property(String source, Object value) {
    }

}
