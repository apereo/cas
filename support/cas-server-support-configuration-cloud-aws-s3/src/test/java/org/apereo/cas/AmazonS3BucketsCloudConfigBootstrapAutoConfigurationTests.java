package org.apereo.cas;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.config.AmazonS3BucketsCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.env.MockEnvironment;
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
 * This is {@link AmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    AmazonS3BucketsCloudConfigBootstrapAutoConfiguration.class
}, properties = {
    "cas.spring.cloud.aws.s3.region=us-east-1",
    "cas.spring.cloud.aws.s3.bucket-name=" + AmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.BUCKET_NAME,
    "cas.spring.cloud.aws.s3.endpoint=" + AmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.s3.credential-access-key=" + AmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.s3.credential-secret-key=" + AmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
class AmazonS3BucketsCloudConfigBootstrapAutoConfigurationTests {
    static final String BUCKET_NAME = "config-bucket";

    static final String ENDPOINT = "http://localhost:4566";

    static final String CREDENTIAL_SECRET_KEY = "test";

    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(AmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(AmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "region", Region.US_EAST_1.id());
        environment.setProperty(AmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(AmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", CREDENTIAL_SECRET_KEY);
        val clientBuilder = S3Client.builder().serviceConfiguration(S3Configuration.Builder::pathStyleAccessEnabled).forcePathStyle(true);
        val builder = new AmazonEnvironmentAwareClientBuilder(AmazonS3BucketsCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val s3Client = builder.build(clientBuilder, S3Client.class);
        deleteBucket(s3Client);
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        val properties = "cas.authn.accept.users=" + STATIC_AUTHN_USERS;
        val request = PutObjectRequest.builder().bucket(BUCKET_NAME).key("cas.properties").build();
        s3Client.putObject(request, RequestBody.fromString(properties));
    }

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
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
