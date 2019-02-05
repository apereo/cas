package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class GroovyPrincipalFactoryTests {
    @Test
    public void verifyAction() {
        val factory = PrincipalFactoryUtils.newGroovyPrincipalFactory(new ClassPathResource("PrincipalFactory.groovy"));
        val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertEquals("casuser", p.getId());
        assertEquals(1, p.getAttributes().size());
    }
}
