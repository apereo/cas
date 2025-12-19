package org.apereo.cas.jdbc;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJdbcStoredProcedureAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTest(
    classes = CasJdbcAuthenticationConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.jdbc.procedure[0].procedure-name=sp_authenticate",
        "cas.authn.jdbc.procedure[0].user=postgres",
        "cas.authn.jdbc.procedure[0].password=password",
        "cas.authn.jdbc.procedure[0].driver-class=org.postgresql.Driver",
        "cas.authn.jdbc.procedure[0].url=jdbc:postgresql://localhost:5432/users",
        "cas.authn.jdbc.procedure[0].dialect=org.hibernate.dialect.PostgreSQLDialect"
    })
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@ExtendWith(CasTestExtension.class)
class CasJdbcStoredProcedureAuthenticationTests {
    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    private AuthenticationManager authenticationManager;

    @Test
    void verifyOperation() throws Throwable {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(CoreAuthenticationTestUtils.getService(), credential);
        assertNotNull(authenticationManager.authenticate(transaction));
    }

    @Test
    void verifyFailsOperation() {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("unknown", "Mellon");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(CoreAuthenticationTestUtils.getService(), credential);
        assertThrows(AuthenticationException.class, () -> authenticationManager.authenticate(transaction));
    }
}
