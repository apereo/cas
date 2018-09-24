package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentJdbcConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link JpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {CasConsentJdbcConfiguration.class, RefreshAutoConfiguration.class})
public class JpaConsentRepositoryTests extends BaseConsentRepositoryTests {
}
