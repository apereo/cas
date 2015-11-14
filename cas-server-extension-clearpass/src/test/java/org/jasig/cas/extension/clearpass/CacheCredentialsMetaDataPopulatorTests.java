package org.jasig.cas.extension.clearpass;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link CacheCredentialsMetaDataPopulator}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class CacheCredentialsMetaDataPopulatorTests {

    @Test
    public void verifyAttributePopulationWithPassword() {
        final Authentication auth = TestUtils.getAuthentication();
        final Map<String, String> map = new HashMap<>();
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator(map);

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        populator.populateAttributes(DefaultAuthenticationBuilder.newInstance(auth), c);

        assertTrue(map.containsKey(auth.getPrincipal().getId()));
        assertEquals(map.get(auth.getPrincipal().getId()), c.getPassword());
    }

    @Test
    public void verifyAttributePopulationWithPasswordWithDifferentCredentialsType() {
        final Authentication auth = TestUtils.getAuthentication();
        final Map<String, String> map = new HashMap<>();
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator(map);

        final Credential c = new Credential() {
            @Override
            public String getId() {
                return "something";
            }
        };

        if (populator.supports(c)) {
            populator.populateAttributes(DefaultAuthenticationBuilder.newInstance(auth), c);
        }

        assertEquals(map.size(), 0);

    }

}
