package org.apereo.cas;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.config.AmazonS3BucketsCloudConfigBootstrapConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.SkipMd5CheckStrategy;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.env.MockEnvironment;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonS3BucketsCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AmazonS3BucketsCloudConfigBootstrapConfiguration.class
}, properties = {
    "cas.spring.cloud.aws.s3.bucketName=" + AmazonS3BucketsCloudConfigBootstrapConfigurationTests.BUCKET_NAME,
    "cas.spring.cloud.aws.s3.endpoint=" + AmazonS3BucketsCloudConfigBootstrapConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.s3.credentialAccessKey=" + AmazonS3BucketsCloudConfigBootstrapConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.s3.credentialSecretKey=" + AmazonS3BucketsCloudConfigBootstrapConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnabledIfPortOpen(port = 4572)
@Tag("AmazonWebServices")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AmazonS3BucketsCloudConfigBootstrapConfigurationTests {
    static final String BUCKET_NAME = "config-bucket";
    static final String ENDPOINT = "http://127.0.0.1:4572";
    static final String CREDENTIAL_SECRET_KEY = "test";
    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    static {
        System.setProperty(SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY, "true");
        System.setProperty(SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY, "true");
    }
    
    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(AmazonS3BucketsCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(AmazonS3BucketsCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialAccessKey", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(AmazonS3BucketsCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialSecretKey", CREDENTIAL_SECRET_KEY);

        val builder = new AmazonEnvironmentAwareClientBuilder(AmazonS3BucketsCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val s3Client = builder.build(AmazonS3ClientBuilder.standard(), AmazonS3.class);

        deleteBucket(s3Client);

        s3Client.createBucket(BUCKET_NAME);
        val properties = "cas.authn.accept.users=" + STATIC_AUTHN_USERS;
        val request = new PutObjectRequest(BUCKET_NAME, "cas.properties",
            new ByteArrayInputStream(properties.getBytes(StandardCharsets.UTF_8)), new ObjectMetadata());
        s3Client.putObject(request);
    }

    private static void deleteBucket(final AmazonS3 s3Client) {
        if (!s3Client.doesBucketExistV2(BUCKET_NAME)) {
            return;
        }
        var objectListing = s3Client.listObjects(BUCKET_NAME);
        while (true) {
            for (val next : objectListing.getObjectSummaries()) {
                s3Client.deleteObject(BUCKET_NAME, next.getKey());
            }
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
        s3Client.deleteBucket(BUCKET_NAME);
    }

    @Test
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
