package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link JsonResourceSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Tag("FileSystem")
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.json.location=classpath:/surrogates.json")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JsonResourceSurrogateAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService service;
}
