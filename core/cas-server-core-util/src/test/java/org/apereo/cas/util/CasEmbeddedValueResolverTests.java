package org.apereo.cas.util;

import org.apereo.cas.CasEmbeddedValueResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEmbeddedValueResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
})
@WebAppConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class CasEmbeddedValueResolverTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void verifyDuration() {
        val resolver = new CasEmbeddedValueResolver(applicationContext);
        val result = resolver.resolveStringValue("PT30S");
        assertEquals("30000", result);
    }

    @Test
    public void verifyNoDuration() {
        val resolver = new CasEmbeddedValueResolver(applicationContext);
        val result = resolver.resolveStringValue("1000");
        assertEquals("1000", result);
    }
}
