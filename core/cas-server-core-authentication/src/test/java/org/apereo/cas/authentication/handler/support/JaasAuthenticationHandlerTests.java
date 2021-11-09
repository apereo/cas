package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.jaas.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.FailedLoginException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationHandler")
public class JaasAuthenticationHandlerTests {
    private File fileName;

    @BeforeEach
    public void initialize() throws Exception {
        val resource = new ClassPathResource("jaas.conf");
        this.fileName = new File(FileUtils.getTempDirectory(), "jaas-custom.conf");
        try (val writer = Files.newBufferedWriter(fileName.toPath(), StandardCharsets.UTF_8)) {
            IOUtils.copy(resource.getInputStream(), writer, Charset.defaultCharset());
            writer.flush();
        }
    }

    @Test
    public void verifyWithValidCredentials() throws Exception {
        val handler = new JaasAuthenticationHandler("JAAS", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0);
        handler.setLoginConfigType("JavaLoginConfig");
        handler.setLoginConfigurationFile(this.fileName);
        handler.setRealm("CAS");
        assertNotNull(handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyFailsCredentials() throws Exception {
        val handler = new JaasAuthenticationHandler("JAAS", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0);
        handler.setLoginConfigType("JavaLoginConfig");
        handler.setLoginConfigurationFile(this.fileName);
        handler.setRealm("CAS");
        handler.setPasswordPolicyHandlingStrategy(null);
        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyWithValidCredentialsPreDefined() throws Exception {
        val handler = new JaasAuthenticationHandler("JAAS", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0);
        handler.setLoginConfigType("JavaLoginConfig");
        handler.setLoginConfigurationFile(this.fileName);
        handler.setRealm("ACCTS");
        assertNotNull(handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon")));
    }
}
