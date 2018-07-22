package org.apereo.cas.authentication;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;

import lombok.val;
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
public class SurrogateAuthenticationMetaDataPopulatorTests {
    @Test
    public void verifyAction() {
        val p = new SurrogateAuthenticationMetaDataPopulator();
        assertFalse(p.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));

        val c = new SurrogateUsernamePasswordCredential();
        c.setSurrogateUsername("cassurrogate");
        c.setUsername("casuser");
        c.setPassword("password");

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        p.populateAttributes(builder, DefaultAuthenticationTransaction.of(c));
        val auth = builder.build();
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED));
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL));
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER));

    }
}
