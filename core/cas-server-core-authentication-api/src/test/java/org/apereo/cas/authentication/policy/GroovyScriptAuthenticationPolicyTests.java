package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyScriptAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
public class GroovyScriptAuthenticationPolicyTests {

    @Test
    public void verifyActionExternalScript() throws Exception {
        val script = "import org.apereo.cas.authentication.*\n"
            + "def run(Object[] args) {"
            + " def principal = args[0]\n"
            + " def logger = args[1]\n"
            + " return Optional.of(new AuthenticationException())\n"
            + '}';

        val scriptFile = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        val p = new GroovyScriptAuthenticationPolicy("file:" + scriptFile.getCanonicalPath());
        assertThrows(GeneralSecurityException.class,
            () -> p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication(),
                new LinkedHashSet<>(), mock(ConfigurableApplicationContext.class), Optional.empty()));
    }

    @Test
    public void verifyResumeOnFailureExternal() throws Exception {
        val script = "def shouldResumeOnFailure(Object[] args) {"
            + " def failure = args[0] \n"
            + " return failure != null \n"
            + '}';

        val scriptFile = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        val p = new GroovyScriptAuthenticationPolicy("file:" + scriptFile.getCanonicalPath());
        assertTrue(p.shouldResumeOnFailure(new RuntimeException()));
    }

    @Test
    public void verifyBadFile() throws Exception {
        val script = "def shouldResumeOnFailure(Object[] args) {"
            + " def failure = args[0] \n"
            + " return failure != null \n"
            + '}';
        val p = new GroovyScriptAuthenticationPolicy(script);
        assertThrows(IllegalArgumentException.class, () -> p.shouldResumeOnFailure(new RuntimeException()));
    }
}
