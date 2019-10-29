package org.apereo.cas.util.spring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpringSpelStringResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class SpringSpelStringResolverTests {
    @Test
    public void verifyOperation() {
        var resolver = new SpringSpelStringValueResolver("${'Hello World'}");
        assertEquals("Hello World", resolver.resolve());

        resolver = new SpringSpelStringValueResolver("Literal Value");
        assertEquals("Literal Value", resolver.resolve());
        
        resolver = new SpringSpelStringValueResolver("${'Hello World'.concat('!')}");
        assertEquals("Hello World!", resolver.resolve());

        System.setProperty("cas.user", "Apereo CAS");
        resolver = new SpringSpelStringValueResolver("${#systemProperties['cas.user']}");
        assertEquals("Apereo CAS", resolver.resolve());

        resolver = new SpringSpelStringValueResolver("${#environmentVars['HOME']}");
        assertNotNull(resolver.resolve());

        System.setProperty("cas.dir", "etc/cas/config");
        resolver = new SpringSpelStringValueResolver("file://${#systemProperties['cas.dir']}/file.json");
        assertEquals("file://etc/cas/config/file.json", resolver.resolve());

    }
}
