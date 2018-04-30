package org.apereo.cas.trusted.authentication.storage;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;


/**
 * This is {@link JsonMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.mfa.trusted.json.location=file:/etc/cas/trusted-device.json")
public class JsonMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {
    @BeforeClass
    @SneakyThrows
    public static void beforeClass() {
        deleteJsonFile();
    }

    @AfterClass
    @SneakyThrows
    public static void afterClass() {
        deleteJsonFile();
    }

    private static void deleteJsonFile() throws IOException {
        final File file = new File("/etc/cas/trusted-device.json");
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }
}
