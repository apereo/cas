package org.apereo.cas.support.wsfederation.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyWsFederationAttributeMutatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
class GroovyWsFederationAttributeMutatorTests {
    @Test
    void verifyAction() throws Throwable {
        val location = new ClassPathResource("GroovyWsFedMutator.groovy");
        val attributeMutator = new GroovyWsFederationAttributeMutator(new WatchableGroovyScriptResource(location));
        val results = attributeMutator.modifyAttributes(CoreAuthenticationTestUtils.getAttributes());
        assertEquals(1, results.size());
        assertTrue(results.containsKey("mail"));
    }
}
