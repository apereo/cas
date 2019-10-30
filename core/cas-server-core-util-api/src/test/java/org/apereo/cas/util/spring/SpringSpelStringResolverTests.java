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
        var resolver = new SpringSpelStringValueResolver();

        assertEquals("Hello World", resolver.resolve("${'Hello World'}"));
        assertEquals("Literal Value", resolver.resolve("Literal Value"));
        assertEquals("Hello World!", resolver.resolve("${'Hello World'.concat('!')}"));

        System.setProperty("cas.user", "Apereo CAS");
        assertEquals("Apereo CAS", resolver.resolve("${#systemProperties['cas.user']}"));
        assertNotNull(resolver.resolve("${#environmentVars['HOME']}"));

        System.setProperty("cas.dir", "etc/cas/config");
        assertEquals("file://etc/cas/config/file.json",
            resolver.resolve("file://${#systemProperties['cas.dir']}/file.json"));

    }
}
