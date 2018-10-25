package org.apereo.cas.digest;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
public class DefaultDigestHashedCredentialRetrieverTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyCanFindAnExistingUser() throws Exception {
        val expectedPassword = "password";
        val credentialRetriever = new DefaultDigestHashedCredentialRetriever(
            Collections.singletonMap("user", expectedPassword));

        val credential = credentialRetriever.findCredential("user", "ignored");

        assertEquals(expectedPassword, credential);
    }

    @Test
    public void verifyAnExceptionIsThrownIfUsedDoesNotExist() throws Exception {
        val username = "user";
        val credentialRetriever = new DefaultDigestHashedCredentialRetriever(
            Collections.singletonMap("anotherUsername", "password"));

        thrown.expect(AccountNotFoundException.class);


        credentialRetriever.findCredential(username, "ignored");
    }
}
