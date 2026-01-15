package org.apereo.cas;

import module java.base;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.config.CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import org.yaml.snakeyaml.Yaml;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration.class, properties = {
    "cas.spring.cloud.aws.s3.region=us-east-1",
    "cas.spring.cloud.aws.s3.bucket-name=" + CasAmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.BUCKET_NAME,
    "cas.spring.cloud.aws.s3.endpoint=" + CasAmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.s3.credential-access-key=" + CasAmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.s3.credential-secret-key=" + CasAmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasAmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests {
    static final String BUCKET_NAME = "config-bucket";

    static final String ENDPOINT = "http://localhost:4566";

    static final String CREDENTIAL_SECRET_KEY = "test";

    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableEnvironment environment;
    
    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "region", Region.US_EAST_1.id());
        environment.setProperty(CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", CREDENTIAL_SECRET_KEY);
        val clientBuilder = S3Client.builder().serviceConfiguration(S3Configuration.Builder::pathStyleAccessEnabled).forcePathStyle(true);
        val builder = new AmazonEnvironmentAwareClientBuilder(CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val s3Client = builder.build(clientBuilder, S3Client.class);
        deleteBucket(s3Client);
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        val properties = "cas.authn.accept.users=" + STATIC_AUTHN_USERS;
        var request = PutObjectRequest.builder().bucket(BUCKET_NAME).key("cas.properties").build();
        s3Client.putObject(request, RequestBody.fromString(properties));

        request = PutObjectRequest.builder().bucket(BUCKET_NAME).key("cas.yml").build();
        val yaml = new Yaml();
        val yamlString = yaml.dump(Map.of("server", Map.of("port", 8080)));
        s3Client.putObject(request, RequestBody.fromString(yamlString));
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());

        val propertySource = environment.getPropertySources()
            .stream()
            .filter(source -> source instanceof BootstrapPropertySource<?>)
            .map(BootstrapPropertySource.class::cast)
            .map(BootstrapPropertySource::getDelegate)
            .filter(MutablePropertySource.class::isInstance)
            .map(MutablePropertySource.class::cast)
            .findFirst()
            .orElseThrow();
        propertySource.setProperty("spring.security.user.roles[0]", "ADMIN");
        propertySource.setProperty("server.port", "9090");
        propertySource.setProperty("cas.server.prefix", "https://example.org/cas");
        propertySource.refresh();
        val prefix = environment.getProperty("cas.server.prefix");
        assertEquals("https://example.org/cas", prefix);
        propertySource.removeProperty("server.port");
        assertNull(environment.getProperty("server.port"));
        propertySource.removeAll();
        assertEquals(0, propertySource.getPropertyNames().length);
    }

    private static void deleteBucket(final S3Client s3Client) {
        try {
            val objects = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(BUCKET_NAME).build());
            objects.contents().forEach(object -> s3Client.deleteObject(DeleteObjectRequest.builder().bucket(BUCKET_NAME).key(object.key()).build()));
            s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(BUCKET_NAME).build());
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage());
        }
    }
}
