package org.apereo.cas.util.spring;

import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpringExpressionLanguageValueResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreUtilAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class SpringExpressionLanguageValueResolverTests {
    @Test
    void verifyFormatOperation() {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        assertEquals("Hello World!", resolver.format("Hello ${name}!", Map.of("name", "World")));
        assertEquals("Hello World!", resolver.format("Hello ${#attributes['name']}!", Map.of("attributes", Map.of("name", "World"))));
        assertEquals("Hello ${#unknown}!", resolver.format("Hello ${#unknown}!", Map.of()));
    }

    @Test
    void verifyOperation() {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();

        assertNotNull(resolver.apply("HelloWorld"));

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

        assertNotNull(resolver.resolve("${#localDateTime}"));
        assertNotNull(resolver.resolve("${#localDateTimeUtc}"));
        assertNotNull(resolver.resolve("${#localDate}"));
        assertNotNull(resolver.resolve("${#localDateUtc}"));
        assertNotNull(resolver.resolve("${#zonedDateTime}"));
        assertNotNull(resolver.resolve("${#zonedDateTimeUtc}"));
        
        assertNotNull(resolver.resolve("${#applicationContext.get().id}"));
    }
}
