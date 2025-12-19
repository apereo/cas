package org.apereo.cas.impl.token;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJpaPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@Getter
@Tag("JDBC")
@ImportAutoConfiguration({
    CasHibernateJpaAutoConfiguration.class,
    CasJpaPasswordlessAuthenticationAutoConfiguration.class
})
@TestPropertySource(properties = "cas.jdbc.show-sql=false")
class JpaPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Test
    void verifyAction() {
        val uid = UUID.randomUUID().toString();

        val passwordlessUserAccount = PasswordlessUserAccount.builder().username(uid).build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(uid).build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);

        assertTrue(passwordlessTokenRepository.findToken(uid).isEmpty());

        val savedToken = passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        assertTrue(passwordlessTokenRepository.findToken(uid).isPresent());

        passwordlessTokenRepository.deleteToken(savedToken);
        assertTrue(passwordlessTokenRepository.findToken(uid).isEmpty());
    }


    @Test
    void verifyCleaner() {
        val uid = UUID.randomUUID().toString();
        val passwordlessUserAccount = PasswordlessUserAccount.builder().username(uid).build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(uid).build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);

        passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        assertTrue(passwordlessTokenRepository.findToken(uid).isPresent());

        passwordlessTokenRepository.clean();

        assertTrue(passwordlessTokenRepository.findToken(uid).isEmpty());
    }
}
