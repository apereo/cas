package org.apereo.cas.aws.s3.services;

import module java.base;
import org.apereo.cas.config.CasAmazonS3ServiceRegistryAutoConfiguration;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonS3ServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    CasAmazonS3ServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
}, properties = {
    "cas.service-registry.amazon-s3.endpoint=http://127.0.0.1:4566",
    "cas.service-registry.amazon-s3.region=us-east-1",
    "cas.service-registry.amazon-s3.credential-access-key=test",
    "cas.service-registry.amazon-s3.credential-secret-key=test",
    "cas.service-registry.amazon-s3.path-style-enabled=true"
})
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AmazonS3ServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    private ServiceRegistry newServiceRegistry;

    @Autowired
    @Qualifier("amazonS3ServiceRegistryClient")
    private S3Client amazonS3ServiceRegistryClient;

    @Test
    void verifyFailsOp() {
        assertNotNull(amazonS3ServiceRegistryClient);
        val service = mock(RegisteredService.class);
        when(service.getId()).thenThrow(new RuntimeException());
        val res = newServiceRegistry.save(service);
        assertEquals(res, service);
        assertFalse(newServiceRegistry.delete(service));
    }

    @Test
    void verifyUnrelatedBucketsAreIgnored() {
        val serviceId = RandomUtils.nextLong(100_000, 1_000_000);
        val bucketName = "casunrelated%sbucket".formatted(serviceId);
        val objectKey = "sp-metadata.xml";
        amazonS3ServiceRegistryClient.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        try {
            amazonS3ServiceRegistryClient.putObject(PutObjectRequest.builder().bucket(bucketName).key(objectKey).build(),
                RequestBody.fromString("<EntityDescriptor/>"));
            assertTrue(newServiceRegistry.load().stream().allMatch(Objects::nonNull));
            assertNull(newServiceRegistry.findServiceById(serviceId));
            newServiceRegistry.deleteAll();
            val objects = amazonS3ServiceRegistryClient.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
            assertEquals(1, objects.keyCount());
        } finally {
            amazonS3ServiceRegistryClient.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
            amazonS3ServiceRegistryClient.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
        }
    }
}
