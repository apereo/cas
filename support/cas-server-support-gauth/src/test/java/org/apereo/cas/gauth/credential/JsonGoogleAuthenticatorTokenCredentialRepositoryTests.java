package org.apereo.cas.gauth.credential;

import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

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
        "spring.mail.port=25000"
    })
@Getter
@Tag("MFA")
public class JsonGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;
}
