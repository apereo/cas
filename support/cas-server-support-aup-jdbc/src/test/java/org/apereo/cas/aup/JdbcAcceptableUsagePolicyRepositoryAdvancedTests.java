package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

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
public class JdbcAcceptableUsagePolicyRepositoryAdvancedTests extends BaseJdbcAcceptableUsagePolicyRepositoryTests {
    @BeforeEach
    public void initialize() throws SQLException {
        try (val c = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
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
    public void verifyRepositoryActionWithAdvancedConfig() {
        verifyRepositoryAction("casuser",
            CollectionUtils.wrap("aupAccepted", List.of("false"), "email", List.of("casuser@example.org")));
    }

    @Test
    public void verifySubmitWithoutAuthn() {
        val c = getCredential("casuser");
        val context = getRequestContext("casuser", Map.of(), c);
        WebUtils.putAuthentication(null, context);
        assertFalse(getAcceptableUsagePolicyRepository().submit(context));
    }

    @Test
    public void verifyRepositoryPolicyText() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policy.setMessageCode("aup.code");
        policy.setText("aup text here");
        service.setAcceptableUsagePolicy(policy);
        verifyFetchingPolicy(service, RegisteredServiceTestUtils.getAuthentication(), true);
    }

    @Test
    public void verifyRepositoryPolicyNoService() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        verifyFetchingPolicy(service, RegisteredServiceTestUtils.getAuthentication(), false);
    }

    @Test
    public void verifyRepositoryPolicyNoServiceViaAttr() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser");
        verifyFetchingPolicy(service, RegisteredServiceTestUtils.getAuthentication(principal), false);
    }

    @Test
    public void determinePrincipalIdWithAdvancedConfig() {
        val principalId = determinePrincipalId("casuser",
            CollectionUtils.wrap("aupAccepted", List.of("false"), "email", List.of("CASuser@example.org")));
        assertEquals("CASuser@example.org", principalId);
    }

    @Test
    public void raiseMissingPrincipalAttributeError() {
        val exception = assertThrows(IllegalStateException.class,
            () -> raiseException(CollectionUtils.wrap("aupAccepted", List.of("false"), "wrong-attribute",
                List.of("CASuser@example.org"))));
        assertTrue(exception.getMessage().contains("cannot be found"));
    }

    @Test
    public void raiseEmptyPrincipalAttributeError() {
        val exception = assertThrows(IllegalStateException.class,
            () -> raiseException(CollectionUtils.wrap("aupAccepted", List.of("false"), "email", new ArrayList<>())));
        assertTrue(exception.getMessage().contains("empty or multi-valued with an empty element"));
    }

    @Override
    public boolean hasLiveUpdates() {
        return true;
    }

    private void raiseException(final Map<String, List<Object>> profileAttributes) {
        val aupProperties = casProperties.getAcceptableUsagePolicy();
        val jdbcAupRepository = new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
            aupProperties,
            acceptableUsagePolicyDataSource,
            jdbcAcceptableUsagePolicyTransactionTemplate);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val principal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), profileAttributes);
        val auth = CoreAuthenticationTestUtils.getAuthentication(principal);
        WebUtils.putAuthentication(auth, context);
        assertNotNull(jdbcAupRepository.determinePrincipalId(principal));
    }
}
