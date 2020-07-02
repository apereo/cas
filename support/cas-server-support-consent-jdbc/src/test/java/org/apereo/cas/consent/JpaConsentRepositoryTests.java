package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentJdbcConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    CasConsentJdbcConfiguration.class,
    CasHibernateJpaConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.jdbc.show-sql=true",
    "cas.authn.mfa.yubikey.jpa.ddl-auto=create-drop"
})
@Getter
@Tag("JDBC")
public class JpaConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;

    @Test
    public void verifyBadDelete() {
        assertFalse(repository.deleteConsentDecision(-1, null));
    }

}
