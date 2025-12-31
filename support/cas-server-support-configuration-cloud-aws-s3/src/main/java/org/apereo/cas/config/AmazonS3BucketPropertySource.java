package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.util.LoggingUtils;
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
public class AmazonS3BucketPropertySource extends EnumerablePropertySource<S3Client> implements MutablePropertySource<S3Client> {
    private final Map<String, S3Property> properties = new ConcurrentHashMap<>();

    private final String bucketName;

    public AmazonS3BucketPropertySource(final String name, final S3Client s3Client, final String bucketName) {
        super(name, s3Client);
        this.bucketName = bucketName;
        refresh();
    }

    @Override
    public void refresh() {
        val result = getSource().listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        val objects = result.contents();
        objects.forEach(obj -> {
            val objectKey = obj.key();
            LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);
            try (val is = getSource().getObject(GetObjectRequest.builder().bucket(bucketName).key(objectKey).build())) {
                if (objectKey.endsWith("properties")) {
                    val props = new Properties();
                    props.load(is);
                    props.forEach((key, value) -> properties.put(key.toString(), new S3Property(objectKey, value)));
                } else if (objectKey.endsWith("yml") || objectKey.endsWith("yaml")) {
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
            getSource().deleteObject(builder -> builder.bucket(bucketName).key(objectKey));
            properties.remove(name);
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
        val request = PutObjectRequest.builder().key(configSource).bucket(bucketName).build();
        if (configSource.endsWith("yml") || configSource.endsWith("yaml")) {
            val yamlMap = CasCoreConfigurationUtils.convertToNestedMap(Map.of(name, value));
            val yaml = new Yaml();
            getSource().putObject(request, RequestBody.fromString(yaml.dump(yamlMap)));
            properties.put(name, new S3Property(configSource, value));
        } else if (configSource.endsWith("properties")) {
            getSource().putObject(request, RequestBody.fromString(name + '=' + value));
            properties.put(name, new S3Property(configSource, value));
        }
        return this;
    }

    private record S3Property(String source, Object value) {
    }

}
