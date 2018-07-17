package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class GroovyPrincipalFactoryTests {
    @Test
    public void verifyAction() {
        val factory = PrincipalFactoryUtils.newGroovyPrincipalFactory(new ClassPathResource("PrincipalFactory.groovy"));
        val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertTrue(p.getId().equals("casuser"));
        assertEquals(1, p.getAttributes().size());
    }
}
