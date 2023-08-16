package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("GroovyAuthentication")
class GroovyPrincipalFactoryTests {
    @Test
    void verifyAction() throws Throwable {
        val factory = PrincipalFactoryUtils.newGroovyPrincipalFactory(new ClassPathResource("PrincipalFactory.groovy"));
        val principal = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertEquals("casuser", principal.getId());
        assertEquals(1, principal.getAttributes().size());
    }
}
