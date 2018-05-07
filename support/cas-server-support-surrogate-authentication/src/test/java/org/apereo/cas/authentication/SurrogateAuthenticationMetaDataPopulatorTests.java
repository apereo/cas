package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogateAuthenticationMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@Slf4j
public class SurrogateAuthenticationMetaDataPopulatorTests {
    @Test
    public void verifyAction() {
        final var p = new SurrogateAuthenticationMetaDataPopulator();
        assertFalse(p.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));

        final var c = new SurrogateUsernamePasswordCredential();
        c.setSurrogateUsername("cassurrogate");
        c.setUsername("casuser");
        c.setPassword("password");

        final var builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        p.populateAttributes(builder, DefaultAuthenticationTransaction.of(c));
        final var auth = builder.build();
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED));
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL));
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER));

    }
}
