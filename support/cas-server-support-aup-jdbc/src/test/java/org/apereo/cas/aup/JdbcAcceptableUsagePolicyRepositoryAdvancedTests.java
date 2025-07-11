package org.apereo.cas.aup;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryAdvancedTests}.
 *
 * @author Martin Böhmer
 * @since 5.3.8
 */
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.core.aup-attribute-name=aupAccepted",
    "cas.acceptable-usage-policy.core.aup-policy-terms-attribute-name=cn",
    "cas.acceptable-usage-policy.jdbc.table-name=users_table",
    "cas.acceptable-usage-policy.jdbc.aup-column=aup",
    "cas.acceptable-usage-policy.jdbc.principal-id-column=mail",
    "cas.acceptable-usage-policy.jdbc.principal-id-attribute=email",
    "cas.acceptable-usage-policy.jdbc.sql-update=UPDATE %s SET %s=TRUE WHERE lower(%s)=lower(?)"
})
@Tag("JDBC")
class JdbcAcceptableUsagePolicyRepositoryAdvancedTests extends BaseJdbcAcceptableUsagePolicyRepositoryTests {
    @BeforeEach
    void initialize() throws SQLException {
        try (val connection = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val s = connection.createStatement()) {
                connection.setAutoCommit(true);
                s.execute("CREATE TABLE users_table (id int primary key, username varchar(255), mail varchar(255), aup boolean)");
                s.execute("INSERT INTO users_table (id, username, mail, aup) values (100, 'casuser', 'casuser@example.org', false);");
            }
        }
    }

    @AfterEach
    public void cleanup() throws SQLException {
        try (val c = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("DROP TABLE users_table;");
            }
        }
    }

    @Test
    void verifyRepositoryActionWithAdvancedConfig() throws Throwable {
        verifyRepositoryAction("casuser",
            CollectionUtils.wrap("aupAccepted", List.of("false"), "email", List.of("casuser@example.org")));
    }

    @Test
    void verifySubmitWithoutAuthn() throws Throwable {
        val c = getCredential("casuser");
        val context = getRequestContext("casuser", Map.of(), c);
        WebUtils.putAuthentication((Authentication) null, context);
        assertFalse(getAcceptableUsagePolicyRepository().submit(context));
    }

    @Test
    void verifyRepositoryPolicyText() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policy.setMessageCode("aup.code");
        policy.setText("aup text here");
        service.setAcceptableUsagePolicy(policy);
        verifyFetchingPolicy(service, RegisteredServiceTestUtils.getAuthentication(), true);
    }

    @Test
    void verifyRepositoryPolicyNoService() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        verifyFetchingPolicy(service, RegisteredServiceTestUtils.getAuthentication(), false);
    }

    @Test
    void verifyRepositoryPolicyNoServiceViaAttr() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser");
        verifyFetchingPolicy(service, RegisteredServiceTestUtils.getAuthentication(principal), false);
    }

    @Test
    void determinePrincipalIdWithAdvancedConfig() throws Throwable {
        val principalId = determinePrincipalId("casuser",
            CollectionUtils.wrap("aupAccepted", List.of("false"), "email", List.of("CASuser@example.org")));
        assertEquals("CASuser@example.org", principalId);
    }

    @Test
    void raiseMissingPrincipalAttributeError() {
        val exception = assertThrows(IllegalStateException.class,
            () -> raiseException(CollectionUtils.wrap("aupAccepted", List.of("false"), "wrong-attribute",
                List.of("CASuser@example.org"))));
        assertTrue(exception.getMessage().contains("cannot be found"));
    }

    @Test
    void raiseEmptyPrincipalAttributeError() {
        val exception = assertThrows(IllegalStateException.class,
            () -> raiseException(CollectionUtils.wrap("aupAccepted", List.of("false"), "email", new ArrayList<>())));
        assertTrue(exception.getMessage().contains("empty or multi-valued with an empty element"));
    }

    @Override
    public boolean hasLiveUpdates() {
        return true;
    }

    private void raiseException(final Map<String, List<Object>> profileAttributes) throws Exception {
        val aupProperties = casProperties.getAcceptableUsagePolicy();
        val jdbcAupRepository = new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
            aupProperties,
            acceptableUsagePolicyDataSource,
            jdbcAcceptableUsagePolicyTransactionTemplate);

        val context = MockRequestContext.create(applicationContext);
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val principal = CoreAuthenticationTestUtils.getPrincipal(credentials.getId(), profileAttributes);
        val auth = CoreAuthenticationTestUtils.getAuthentication(principal);
        WebUtils.putAuthentication(auth, context);
        assertNotNull(jdbcAupRepository.determinePrincipalId(principal));
    }
}
