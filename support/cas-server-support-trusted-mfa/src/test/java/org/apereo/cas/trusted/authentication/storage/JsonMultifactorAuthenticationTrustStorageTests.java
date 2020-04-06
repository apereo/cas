package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;


/**
 * This is {@link JsonMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@TestPropertySource(properties = "cas.authn.mfa.trusted.json.location=file:/tmp/trusted-device.json")
@Tag("FileSystem")
@Getter
public class JsonMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @BeforeAll
    @SneakyThrows
    public static void beforeClass() {
        deleteJsonFile();
    }

    @AfterAll
    @SneakyThrows
    public static void afterClass() {
        deleteJsonFile();
    }

    private static void deleteJsonFile() throws IOException {
        val file = new File("/tmp/trusted-device.json");
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }
}
