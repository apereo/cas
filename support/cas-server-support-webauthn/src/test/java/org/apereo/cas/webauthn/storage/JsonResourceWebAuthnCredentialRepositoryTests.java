package org.apereo.cas.webauthn.storage;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

/**
 * This is {@link JsonResourceWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("FileSystem")
@TestPropertySource(properties = "cas.authn.mfa.web-authn.json.location=file:/tmp/webauthn-devices.json")
public class JsonResourceWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {

    @BeforeAll
    public static void bootstrap() {
        FileUtils.deleteQuietly(new File("/tmp/webauthn-devices.json"));
    }

    @AfterAll
    public static void cleanUp() {
        FileUtils.deleteQuietly(new File("/tmp/webauthn-devices.json"));
    }
}
