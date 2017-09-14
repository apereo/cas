package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.U2FJpaConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link U2FJpaDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {U2FJpaConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class U2FJpaDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository u2fDeviceRepository;


    @Override
    protected U2FDeviceRepository getDeviceRepository() {
        return this.u2fDeviceRepository;
    }
}
