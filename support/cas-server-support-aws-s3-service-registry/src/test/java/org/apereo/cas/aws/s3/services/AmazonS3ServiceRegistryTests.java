package org.apereo.cas.aws.s3.services;

import org.apereo.cas.config.AmazonS3ServiceRegistryConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link AmazonS3ServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    AmazonS3ServiceRegistryConfiguration.class,
    CasCoreNotificationsConfiguration.class,
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
public class AmazonS3ServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry newServiceRegistry;
}
