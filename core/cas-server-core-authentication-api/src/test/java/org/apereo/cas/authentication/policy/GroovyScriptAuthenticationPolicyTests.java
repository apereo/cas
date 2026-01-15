package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyScriptAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationPolicy")
class GroovyScriptAuthenticationPolicyTests {

    @Test
    void verifyRejectAuthnWithCredential() throws Throwable {
        val scriptFile = Files.createTempFile("script3", ".groovy").toFile();
        val script = IOUtils.toString(new ClassPathResource("GroovyAuthnPolicy.groovy").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        val policy = new GroovyScriptAuthenticationPolicy("file:" + scriptFile.getCanonicalPath());
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        authentication.getCredentials().getFirst().getCredentialMetadata().putProperty("mustFail", "true");
        assertThrows(GeneralSecurityException.class, () -> policy.isSatisfiedBy(authentication, mock(ConfigurableApplicationContext.class)));
    }

    @Test
    void verifyActionExternalScript() throws Throwable {
        val script = """
            import org.apereo.cas.authentication.*
            def run(Object[] args) {
               def (authentication, context, applicationContext, logger) = args
               return Optional.of(new AuthenticationException())
            }""";

        val scriptFile = Files.createTempFile("script1", ".groovy").toFile();
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        val policy = new GroovyScriptAuthenticationPolicy("file:" + scriptFile.getCanonicalPath());
        assertThrows(GeneralSecurityException.class,
            () -> policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication(), mock(ConfigurableApplicationContext.class)));
    }

    @Test
    void verifyResumeOnFailureExternal() throws Throwable {
        val script = """
            def shouldResumeOnFailure(Object[] args) { def failure = args[0]\s
              return failure != null\s
            }""";

        val scriptFile = Files.createTempFile("script2", ".groovy").toFile();
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        val policy = new GroovyScriptAuthenticationPolicy("file:" + scriptFile.getCanonicalPath());
        assertTrue(policy.shouldResumeOnFailure(new RuntimeException()));
    }

    @Test
    void verifyResumeOnFailureClasspath() {
        val policy = new GroovyScriptAuthenticationPolicy("classpath:/GroovyAuthenticationPolicy.groovy");
        assertFalse(policy.shouldResumeOnFailure(new RuntimeException()));
        assertTrue(policy.shouldResumeOnFailure(new AccountNotFoundException()));
    }

    @Test
    void verifyBadFile() {
        val policy = new GroovyScriptAuthenticationPolicy("unknown-file");
        assertFalse(policy.shouldResumeOnFailure(new RuntimeException()));
    }
}
