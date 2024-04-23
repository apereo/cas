package org.apereo.cas.web.flow.passwordless;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.config.CasSurrogateAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogatePasswordlessAuthenticationRequestParserTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    CasSurrogateAuthenticationWebflowAutoConfiguration.class,
    BaseSurrogateAuthenticationTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@Tag("Delegation")
class SurrogatePasswordlessAuthenticationRequestParserTests extends BaseSurrogateAuthenticationTests {
    @Autowired
    @Qualifier(PasswordlessRequestParser.BEAN_NAME)
    private PasswordlessRequestParser passwordlessRequestParser;

    @Test
    void verifySurrogateRequest() throws Throwable {
        val results = passwordlessRequestParser.parse("user3+casuser");
        assertEquals("casuser", results.getUsername());
    }

    @Test
    void verifyDefaultRequest() throws Throwable {
        val results = passwordlessRequestParser.parse("casuser@example.org");
        assertEquals("casuser@example.org", results.getUsername());
    }
}
