package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentJdbcConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

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
    "cas.jdbc.showSql=true",
    "cas.authn.mfa.yubikey.jpa.ddlAuto=create-drop"
})
@Getter
@Tag("JDBC")
public class JpaConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
