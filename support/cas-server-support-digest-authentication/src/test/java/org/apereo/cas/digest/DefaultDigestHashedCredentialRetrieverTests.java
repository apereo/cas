package org.apereo.cas.digest;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
@Tag("Simple")
public class DefaultDigestHashedCredentialRetrieverTests {

    @Test
    @SneakyThrows
    public void verifyCanFindAnExistingUser() {
        val expectedPassword = "password";
        val credentialRetriever = new DefaultDigestHashedCredentialRetriever(
            Collections.singletonMap("user", expectedPassword));

        val credential = credentialRetriever.findCredential("user", "ignored");

        assertEquals(expectedPassword, credential);
    }

    @Test
    public void verifyAnExceptionIsThrownIfUsedDoesNotExist() {
        val username = "user";
        val credentialRetriever = new DefaultDigestHashedCredentialRetriever(
            Collections.singletonMap("anotherUsername", "password"));

        assertThrows(AccountNotFoundException.class, () -> credentialRetriever.findCredential(username, "ignored"));
    }
}
