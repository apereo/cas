package org.apereo.cas.digest;

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
@Tag("Authentication")
public class DefaultDigestHashedCredentialRetrieverTests {

    @Test
    public void verifyCanFindAnExistingUser() throws Exception{
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
