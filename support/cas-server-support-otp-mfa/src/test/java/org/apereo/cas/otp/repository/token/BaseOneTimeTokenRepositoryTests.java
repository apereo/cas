package org.apereo.cas.otp.repository.token;

import org.apereo.cas.authentication.OneTimeToken;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link BaseOneTimeTokenRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseOneTimeTokenRepositoryTests {

    public static final String CASUSER = "casuser";

    @Test
    public void verifyTokenSave() {
        val token = new OneTimeToken(1234, CASUSER);
        val repository = getRepository();
        repository.store(token);
        repository.store(token);
        assertEquals(2, repository.count(CASUSER));
        repository.clean();
        assertTrue(repository.exists(CASUSER, 1234));
        repository.remove(CASUSER);
        repository.remove(1234);
        repository.remove(CASUSER, 1234);
        assertNull(repository.get(CASUSER, 1234));
        assertEquals(0, repository.count());
    }

    public abstract OneTimeTokenRepository getRepository();
}
