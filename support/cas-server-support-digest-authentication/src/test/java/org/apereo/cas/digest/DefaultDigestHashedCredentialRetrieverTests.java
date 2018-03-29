package org.apereo.cas.digest;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DefaultDigestHashedCredentialRetrieverTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyCanFindAnExistingUser() throws Exception {
        final var expectedPassword = "password";
        final var credentialRetriever = new DefaultDigestHashedCredentialRetriever(
                Collections.singletonMap("user", expectedPassword));

        final var credential = credentialRetriever.findCredential("user", "ignored");

        assertEquals(expectedPassword, credential);
    }

    @Test
    public void verifyAnExceptionIsThrownIfUsedDoesNotExist() throws Exception {
        final var username = "user";
        final var credentialRetriever = new DefaultDigestHashedCredentialRetriever(
                Collections.singletonMap("anotherUsername", "password"));

        thrown.expect(AccountNotFoundException.class);


        credentialRetriever.findCredential(username, "ignored");
    }
}
