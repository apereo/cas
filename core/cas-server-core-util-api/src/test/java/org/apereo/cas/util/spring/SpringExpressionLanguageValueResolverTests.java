package org.apereo.cas.util.spring;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpringExpressionLanguageValueResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class SpringExpressionLanguageValueResolverTests {
    @Test
    public void verifyOperation() {
        var resolver = SpringExpressionLanguageValueResolver.getInstance();

        assertEquals("Hello World", resolver.resolve("${'Hello World'}"));
        assertEquals("Literal Value", resolver.resolve("Literal Value"));
        assertEquals("Hello World!", resolver.resolve("${'Hello World'.concat('!')}"));

        System.setProperty("cas.user", "Apereo CAS");
        assertEquals("Apereo CAS", resolver.resolve("${#systemProperties['cas.user']}"));
        assertNotNull(resolver.resolve("${#environmentVars['HOME']}"));
        assertNotNull(resolver.resolve("${#uuid}"));
        assertNotNull(resolver.resolve("${#randomNumber2}"));
        assertNotNull(resolver.resolve("${#randomNumber4}"));
        assertNotNull(resolver.resolve("${#randomNumber6}"));
        assertNotNull(resolver.resolve("${#randomNumber8}"));

        assertNotNull(resolver.resolve("${#randomString4}"));
        assertNotNull(resolver.resolve("${#randomString6}"));
        assertNotNull(resolver.resolve("${#randomString8}"));

        System.setProperty("cas.dir", "etc/cas/config");
        assertEquals("file://etc/cas/config/file.json",
            resolver.resolve("file://${#systemProperties['cas.dir']}/file.json"));

    }
}
