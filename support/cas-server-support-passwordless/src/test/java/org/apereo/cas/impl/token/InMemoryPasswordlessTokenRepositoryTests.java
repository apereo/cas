package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class InMemoryPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    private static final String CAS_USER = "casuser";

    @Autowired
    @Qualifier("passwordlessTokenRepository")
    private PasswordlessTokenRepository repository;

    @Test
    public void verifyToken() {
        val token = repository.createToken(CAS_USER);
        assertTrue(repository.findToken(CAS_USER).isEmpty());

        repository.saveToken(CAS_USER, token);
        assertTrue(repository.findToken(CAS_USER).isPresent());

        repository.deleteToken(CAS_USER, token);
        assertTrue(repository.findToken(CAS_USER).isEmpty());
    }
}
