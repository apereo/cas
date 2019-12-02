package org.apereo.cas.gauth.credential;

import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.gauth.json.location=classpath:/repository.json",
        "spring.mail.host=localhost",
        "spring.mail.port=25000",
        "spring.mail.testConnection=false"
    })
@Getter
public class JsonGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    private static final Resource JSON_FILE = new FileSystemResource(new File(FileUtils.getTempDirectoryPath(), "repository.json"));

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;
}
