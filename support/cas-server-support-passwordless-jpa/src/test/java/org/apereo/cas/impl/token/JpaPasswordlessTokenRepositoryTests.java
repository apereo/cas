package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaPasswordlessAuthenticationConfiguration;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;

import lombok.Getter;
import lombok.val;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

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
@Import({
    CasHibernateJpaConfiguration.class,
    JpaPasswordlessAuthenticationConfiguration.class
})
@TestPropertySource(properties = "cas.jdbc.showSql=true")
public class JpaPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier("passwordlessTokenRepository")
    private PasswordlessTokenRepository repository;

    @Test
    public void verifyAction() {
        val uid = UUID.randomUUID().toString();
        val token = repository.createToken(uid);
        assertTrue(repository.findToken(uid).isEmpty());

        repository.saveToken(uid, token);
        assertTrue(repository.findToken(uid).isPresent());

        repository.deleteToken(uid, token);
        assertTrue(repository.findToken(uid).isEmpty());
    }

    @Test
    public void verifyCleaner() {
        val uid = UUID.randomUUID().toString();
        val token = repository.createToken(uid);
        repository.saveToken(uid, token);
        assertTrue(repository.findToken(uid).isPresent());

        val tt = ZonedDateTime.now(ZoneOffset.UTC).plusHours(5).toInstant().toEpochMilli();
        DateTimeUtils.setCurrentMillisFixed(tt);
        repository.clean();

        assertTrue(repository.findToken(uid).isEmpty());
    }
}
