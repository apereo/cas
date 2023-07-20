package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link GroovySurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Groovy")
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.groovy.location=classpath:/GroovySurrogate.groovy")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Getter
class GroovySurrogateAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService service;
}
