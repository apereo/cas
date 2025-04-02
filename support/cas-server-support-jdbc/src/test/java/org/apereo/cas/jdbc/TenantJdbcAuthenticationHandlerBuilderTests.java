package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.DigestUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantJdbcAuthenticationHandlerBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@SpringBootTest(
    classes = CasJdbcAuthenticationConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@Tag("JDBCAuthentication")
@ExtendWith(CasTestExtension.class)
class TenantJdbcAuthenticationHandlerBuilderTests {
    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier(TenantsManager.BEAN_NAME)
    private TenantsManager tenantsManager;

    @BeforeEach
    void initialize() throws Exception {
        val casProperties = tenantsManager.findTenant("shire").orElseThrow().bindProperties().orElse(null);
        val props = casProperties.getAuthn().getJdbc().getQuery().getFirst();
        val dataSource = JpaBeans.newDataSource(props.getDriverClass(), props.getUser(),
            props.getPassword(), props.getUrl());

        try (val connection = dataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);
                statement.execute("CREATE TABLE IF NOT EXISTS users (id INT NOT NULL, uid VARCHAR(50), psw VARCHAR(512));");
                statement.execute("INSERT INTO users VALUES (1, 'casuser', '" + DigestUtils.sha256("Mellon") + "');");
            }
        }
    }

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.setContextPath("/tenants/shire/login");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(CoreAuthenticationTestUtils.getService(), credential);
        val result = authenticationManager.authenticate(transaction);
        assertNotNull(result);
    }
}
