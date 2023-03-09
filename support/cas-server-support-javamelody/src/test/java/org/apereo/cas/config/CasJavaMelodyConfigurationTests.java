package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CasJavaMelodyConfigurationTests}.
 * TODO: JavaMelody as of this commit is not compatible with Spring Boot 3.
 * The test here is disabled until support is available.
 * See <a href="https://github.com/javamelody/javamelody/issues/1143">this issue</a>.
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("CasConfiguration")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasJavaMelodyConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Disabled
public class CasJavaMelodyConfigurationTests {

    @Test
    public void verifyOperation() {
        throw new UnsupportedOperationException("JavaMelody is not compatible with Spring Boot 3, yet.");
    }
}
