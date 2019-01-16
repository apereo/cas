package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryAdvancedTests}.
 *
 * @author Martin Böhmer
 * @since 5.3.8
 */
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.jdbc.tableName=users_table",
    "cas.acceptableUsagePolicy.aupAttributeName=aupAccepted",
    "cas.acceptableUsagePolicy.jdbc.aupColumn=aup",
    "cas.acceptableUsagePolicy.jdbc.principalIdColumn=mail",
    "cas.acceptableUsagePolicy.jdbc.principalIdAttribute=email",
    "cas.acceptableUsagePolicy.jdbc.sqlUpdateAUP=UPDATE %s SET %s=true WHERE lower(%s)=lower(?)"
})
public class JdbcAcceptableUsagePolicyRepositoryAdvancedTests extends BaseJdbcAcceptableUsagePolicyRepositoryTests {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void initialize() throws Exception {
        try (val c = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("CREATE TABLE users_table (id int primary key, username varchar(255), mail varchar(255), aup boolean)");
                s.execute("INSERT INTO users_table (id, username, mail, aup) values (100, 'casuser', 'casuser@example.org', false);");
            }
        }
    }
    
    @After
    public void cleanup() throws Exception {
        try (val c = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("DROP TABLE users_table;");
            }
        }
    }

    @Test
    public void verifyRepositoryActionWithAdvancedConfig() {
        verifyRepositoryAction("casuser", CollectionUtils.wrap("aupAccepted", "false", "email", "CASuser@example.org"));
    }
    
    @Test
    public void determinePrincipalIdWithAdvancedConfig() {
        val principalId = determinePrincipalId("casuser", CollectionUtils.wrap("aupAccepted", "false", "email", "CASuser@example.org"));
        assertEquals("CASuser@example.org", principalId);
    }
    
    @Test
    public void raiseMissingPrincipalAttributeError() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(Matchers.containsString("cannot be found"));
        raiseException(CollectionUtils.wrap("aupAccepted", "false", "wrong-attribute", "CASuser@example.org"));
    }
    
    @Test
    public void raiseEmptyPrincipalAttributeError() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(Matchers.containsString("empty or multi-valued with an empty element"));
        raiseException(CollectionUtils.wrap("aupAccepted", "false", "email", ""));
    }
    
    private void raiseException(final Map<String, Object> profileAttributes) {
        val aupProperties = casProperties.getAcceptableUsagePolicy();
        val jdbcAupRepository = new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
                aupProperties.getAupAttributeName(), acceptableUsagePolicyDataSource, aupProperties);
        
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val pricipal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), profileAttributes);
        val auth = CoreAuthenticationTestUtils.getAuthentication(pricipal);
        WebUtils.putAuthentication(auth, context);
        
        jdbcAupRepository.determinePrincipalId(context, c);
    }
}
