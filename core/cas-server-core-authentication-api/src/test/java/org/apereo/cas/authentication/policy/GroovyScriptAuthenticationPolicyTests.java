package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import javax.security.auth.login.AccountNotFoundException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyScriptAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("GroovyAuthentication")
class GroovyScriptAuthenticationPolicyTests {

    @Test
    void verifyActionExternalScript() throws Throwable {
        val script = """
            import org.apereo.cas.authentication.*
            def run(Object[] args) { def principal = args[0]
             def logger = args[1]
             return Optional.of(new AuthenticationException())
            }""";

        val scriptFile = Files.createTempFile("script1", ".groovy").toFile();
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        val p = new GroovyScriptAuthenticationPolicy("file:" + scriptFile.getCanonicalPath());
        assertThrows(GeneralSecurityException.class,
            () -> p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication(), mock(ConfigurableApplicationContext.class)));
    }

    @Test
    void verifyResumeOnFailureExternal() throws Throwable {
        val script = """
            def shouldResumeOnFailure(Object[] args) { def failure = args[0]\s
             return failure != null\s
            }""";

        val scriptFile = Files.createTempFile("script2", ".groovy").toFile();
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        val p = new GroovyScriptAuthenticationPolicy("file:" + scriptFile.getCanonicalPath());
        assertTrue(p.shouldResumeOnFailure(new RuntimeException()));
    }

    @Test
    void verifyResumeOnFailureClasspath() throws Throwable {
        val p = new GroovyScriptAuthenticationPolicy("classpath:/GroovyAuthenticationPolicy.groovy");
        assertFalse(p.shouldResumeOnFailure(new RuntimeException()));
        assertTrue(p.shouldResumeOnFailure(new AccountNotFoundException()));
    }

    @Test
    void verifyBadFile() throws Throwable {
        val script = """
            def shouldResumeOnFailure(Object[] args) { def failure = args[0]\s
             return failure != null\s
            }""";
        val p = new GroovyScriptAuthenticationPolicy(script);
        assertThrows(IllegalArgumentException.class, () -> p.shouldResumeOnFailure(new RuntimeException()));
    }
}
