package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.JpaPasswordlessAuthenticationConfiguration;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Getter
@Tag("JDBC")
@Import(JpaPasswordlessAuthenticationConfiguration.class)
public class JpaPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    private static final String CAS_USER = "casuser";

    @Autowired
    @Qualifier("passwordlessTokenRepository")
    private PasswordlessTokenRepository repository;

    @Test
    public void verifyAction() {
        val token = repository.createToken(CAS_USER);
        assertTrue(repository.findToken(CAS_USER).isEmpty());

        repository.saveToken(CAS_USER, token);
        assertTrue(repository.findToken(CAS_USER).isPresent());

        repository.deleteToken(CAS_USER, token);
        assertTrue(repository.findToken(CAS_USER).isEmpty());
    }
}
