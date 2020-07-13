package org.apereo.cas.support.wsfederation.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

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
public class GroovyWsFederationAttributeMutatorTests {
    @Test
    public void verifyAction() {
        val g = new GroovyWsFederationAttributeMutator(new ClassPathResource("GroovyWsFedMutator.groovy"));
        val results = g.modifyAttributes(CoreAuthenticationTestUtils.getAttributes());
        assertEquals(1, results.size());
        assertTrue(results.containsKey("mail"));
    }
}
