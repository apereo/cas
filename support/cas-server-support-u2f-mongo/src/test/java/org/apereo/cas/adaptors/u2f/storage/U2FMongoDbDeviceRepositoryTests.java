package org.apereo.cas.adaptors.u2f.storage;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FMongoDbConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link U2FMongoDbDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {U2FConfiguration.class,
        U2FMongoDbConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class})
@TestPropertySource(locations = "classpath:/mongou2f.properties")
@Slf4j
public class U2FMongoDbDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository u2fDeviceRepository;

    @Override
    protected U2FDeviceRepository getDeviceRepository() {
        return this.u2fDeviceRepository;
    }
}
