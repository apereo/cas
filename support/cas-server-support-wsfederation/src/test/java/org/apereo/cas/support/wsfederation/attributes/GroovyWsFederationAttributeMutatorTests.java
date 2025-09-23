package org.apereo.cas.support.wsfederation.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyWsFederationAttributeMutatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreScriptingAutoConfiguration.class)
class GroovyWsFederationAttributeMutatorTests {
    @Test
    void verifyAction() throws Throwable {
        val groovyResource = new ClassPathResource("GroovyWsFedMutator.groovy");
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val watchableScript = scriptFactory.fromResource(groovyResource);
        val attributeMutator = new GroovyWsFederationAttributeMutator(watchableScript);
        val results = attributeMutator.modifyAttributes(CoreAuthenticationTestUtils.getAttributes());
        assertEquals(1, results.size());
        assertTrue(results.containsKey("mail"));
    }
}
