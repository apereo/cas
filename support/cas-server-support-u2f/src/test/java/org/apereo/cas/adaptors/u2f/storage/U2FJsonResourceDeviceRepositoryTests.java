package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FJsonResourceDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    U2FConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = "cas.authn.mfa.u2f.json.location=file:/tmp/u2f.json")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("FileSystem")
@Getter
public class U2FJsonResourceDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;


    @BeforeAll
    public static void cleanUp() {
        FileUtils.deleteQuietly(new File("/tmp/u2f.json"));
    }

    @Test
    public void verifyOperation() {
        assertNotNull(deviceRepository);
    }
}
