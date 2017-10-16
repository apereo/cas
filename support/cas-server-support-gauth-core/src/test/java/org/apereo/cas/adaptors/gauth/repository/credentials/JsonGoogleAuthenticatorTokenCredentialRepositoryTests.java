package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.Assert.*;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CasCoreUtilConfiguration.class})
public class JsonGoogleAuthenticatorTokenCredentialRepositoryTests {
    private static final Resource JSON_FILE = new FileSystemResource(new File(FileUtils.getTempDirectoryPath(), "repository.json"));

    private IGoogleAuthenticator google;

    @Before
    public void setup() {
        final GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @Test
    public void verifyCreate() throws Exception {
        if (JSON_FILE.exists()) {
            FileUtils.forceDelete(JSON_FILE.getFile());
        }
        final JsonGoogleAuthenticatorTokenCredentialRepository repo = new JsonGoogleAuthenticatorTokenCredentialRepository(JSON_FILE, google);
        final OneTimeTokenAccount acct = repo.create("casuser");
        assertNotNull(acct);
    }

    @Test
    public void verifyGet() throws Exception {
        if (JSON_FILE.exists()) {
            FileUtils.forceDelete(JSON_FILE.getFile());
        }
        final JsonGoogleAuthenticatorTokenCredentialRepository repo = new JsonGoogleAuthenticatorTokenCredentialRepository(JSON_FILE, google);
        OneTimeTokenAccount acct = repo.get("casuser");
        assertNull(acct);
        acct = repo.create("casuser");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
        acct = repo.get("casuser");
        assertNotNull(acct);
    }
}
