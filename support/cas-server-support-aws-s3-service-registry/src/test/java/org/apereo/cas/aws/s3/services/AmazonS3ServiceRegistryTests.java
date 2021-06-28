package org.apereo.cas.aws.s3.services;

import org.apereo.cas.config.AmazonS3ServiceRegistryConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonS3ServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    AmazonS3ServiceRegistryConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.service-registry.amazon-s3.endpoint=http://127.0.0.1:4566",
    "cas.service-registry.amazon-s3.credential-access-key=test",
    "cas.service-registry.amazon-s3.credential-secret-key=test"
})
@EnabledIfPortOpen(port = 4566)
@Tag("AmazonWebServices")
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AmazonS3ServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Autowired
    @Qualifier("amazonS3ServiceRegistryClient")
    private S3Client amazonS3ServiceRegistryClient;

    @Test
    public void verifyFailsOp() {
        assertNotNull(amazonS3ServiceRegistryClient);
        val service = mock(RegisteredService.class);
        when(service.getId()).thenThrow(new RuntimeException());
        val res = newServiceRegistry.save(service);
        assertEquals(res, service);
        assertFalse(newServiceRegistry.delete(service));
    }
}
