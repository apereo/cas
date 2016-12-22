package org.apereo.cas.digest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
public class DefaultDigestHashedCredentialRetrieverTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyCanFindAnExistingUser() throws Exception {
        final String expectedPassword = "password";
        final DefaultDigestHashedCredentialRetriever credentialRetriever = new DefaultDigestHashedCredentialRetriever(
                Collections.singletonMap("user", expectedPassword));

        final String credential = credentialRetriever.findCredential("user", "ignored");

        assertEquals(expectedPassword, credential);
    }

    @Test
    public void verifyAnExceptionIsThrownIfUsedDoesNotExist() throws Exception {
        final String username = "user";
        final DefaultDigestHashedCredentialRetriever credentialRetriever = new DefaultDigestHashedCredentialRetriever(
                Collections.singletonMap("anotherUsername", "password"));

        thrown.expect(AccountNotFoundException.class);
        thrown.expectMessage("Could not locate user account for " + username);

        credentialRetriever.findCredential(username, "ignored");
    }
}
