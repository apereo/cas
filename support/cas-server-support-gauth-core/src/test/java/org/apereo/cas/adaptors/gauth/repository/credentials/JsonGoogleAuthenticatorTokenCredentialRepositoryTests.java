package org.apereo.cas.adaptors.gauth.repository.credentials;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreUtilConfiguration;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.File;

import static org.junit.Assert.*;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class JsonGoogleAuthenticatorTokenCredentialRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final Resource JSON_FILE = new FileSystemResource(new File(FileUtils.getTempDirectoryPath(), "repository.json"));

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private IGoogleAuthenticator google;

    @Before
    public void initialize() {
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @Test
    public void verifyCreate() throws Exception {
        if (JSON_FILE.exists()) {
            FileUtils.forceDelete(JSON_FILE.getFile());
        }
        val repo =
            new JsonGoogleAuthenticatorTokenCredentialRepository(JSON_FILE, google, CipherExecutor.noOpOfStringToString());
        val acct = repo.create("casuser");
        assertNotNull(acct);
    }

    @Test
    public void verifyGet() throws Exception {
        if (JSON_FILE.exists()) {
            FileUtils.forceDelete(JSON_FILE.getFile());
        }
        val repo =
            new JsonGoogleAuthenticatorTokenCredentialRepository(JSON_FILE, google, CipherExecutor.noOpOfStringToString());
        var acct = repo.get("casuser");
        assertNull(acct);
        acct = repo.create("casuser");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
        acct = repo.get("casuser");
        assertNotNull(acct);
    }
}
