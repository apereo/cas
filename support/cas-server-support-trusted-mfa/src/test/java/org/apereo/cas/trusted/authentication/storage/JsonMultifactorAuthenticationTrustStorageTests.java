package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.io.File;

/**
 * This is {@link JsonMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@TestPropertySource(properties = "cas.authn.mfa.trusted.json.location=file:${java.io.tmpdir}/trusted-device.json")
@Tag("FileSystem")
@ExtendWith(CasTestExtension.class)
class JsonMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @BeforeAll
    public static void beforeClass() {
        deleteJsonFile();
    }

    @AfterAll
    public static void afterClass() {
        deleteJsonFile();
    }

    /**
     * Cleanup json file before and after test, quietly because of errors on Windows.
     */
    private static void deleteJsonFile() {
        val file = new File(FileUtils.getTempDirectory(), "trusted-device.json");
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        }
    }
}
