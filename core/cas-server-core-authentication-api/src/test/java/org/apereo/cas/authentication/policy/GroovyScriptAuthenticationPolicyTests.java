package org.apereo.cas.authentication.policy;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyScriptAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
public class GroovyScriptAuthenticationPolicyTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void verifyActionInlinedScriptPasses() throws Exception {
        final var script = "groovy {"
            + " logger.info(principal.id)\n"
            + " return Optional.empty()\n"
            + '}';
        final var p = new GroovyScriptAuthenticationPolicy(resourceLoader, script);
        assertTrue(p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication()));
    }

    @Test
    public void verifyActionInlinedScriptFails() throws Exception {
        final var script = "groovy {"
            + " import org.apereo.cas.authentication.*\n"
            + " logger.info(principal.id)\n"
            + " return Optional.of(new AuthenticationException())\n"
            + '}';
        final var p = new GroovyScriptAuthenticationPolicy(resourceLoader, script);
        thrown.expect(GeneralSecurityException.class);
        p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication());
    }

    @Test
    public void verifyActionExternalScript() throws Exception {
        final var script = "import org.apereo.cas.authentication.*\n"
            + "def run(Object[] args) {"
            + " def principal = args[0]\n"
            + " def logger = args[1]\n"
            + " return Optional.of(new AuthenticationException())\n"
            + '}';

        final var scriptFile = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        FileUtils.write(scriptFile, script, StandardCharsets.UTF_8);
        final var p = new GroovyScriptAuthenticationPolicy(resourceLoader, "file:" + scriptFile.getCanonicalPath());
        thrown.expect(GeneralSecurityException.class);
        p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication());
    }
}
