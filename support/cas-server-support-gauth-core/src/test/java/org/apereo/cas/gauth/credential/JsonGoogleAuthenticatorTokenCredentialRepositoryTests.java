package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.otp.config.OneTimeTokenAuthenticationConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.config.CasCookieConfiguration;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class,
    OneTimeTokenAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreUtilConfiguration.class
    },
    properties = {"cas.authn.mfa.gauth.json.location=classpath:/repository.json"})
@Getter
public class JsonGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    private static final Resource JSON_FILE = new FileSystemResource(new File(FileUtils.getTempDirectoryPath(), "repository.json"));

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;
}
