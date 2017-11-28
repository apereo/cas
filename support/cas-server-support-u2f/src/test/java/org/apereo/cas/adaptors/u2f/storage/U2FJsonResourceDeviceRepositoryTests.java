package org.apereo.cas.adaptors.u2f.storage;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

/**
 * This is {@link U2FJsonResourceDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {U2FConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FJsonResourceDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FJsonResourceDeviceRepositoryTests.class);

    static {
        try {
            final File file = new File(System.getProperty("java.io.tmpdir"), "u2f.json");
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            System.setProperty("cas.authn.mfa.u2f.json.location", "file://" + file.getCanonicalPath());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository u2fDeviceRepository;


    @Override
    protected U2FDeviceRepository getDeviceRepository() {
        return this.u2fDeviceRepository;
    }
}
