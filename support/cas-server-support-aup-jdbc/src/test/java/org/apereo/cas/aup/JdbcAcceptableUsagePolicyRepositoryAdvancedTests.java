package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.hamcrest.Matchers;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Martin Böhmer
 * @since 5.3.8
 */
@TestPropertySource(locations = {"classpath:/jdbc-aup-advanced.properties"})
public class JdbcAcceptableUsagePolicyRepositoryAdvancedTests extends BaseJdbcAcceptableUsagePolicyRepositoryTests {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final Connection c = this.acceptableUsagePolicyDataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("CREATE TABLE users_table (id int primary key, username varchar(255), mail varchar(255), aup boolean)");
        s.execute("INSERT INTO users_table (id, username, mail, aup) values (100, 'casuser', 'casuser@example.org', false);");
        c.close();
    }
    
    @After
    public void tearDown() throws Exception {
        final Connection c = this.acceptableUsagePolicyDataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("DROP TABLE users_table;");
        c.close();
    }

    @Test
    public void verifyActionWithAdvancedConfig() {
        verifyAction("casuser", CollectionUtils.wrap("aupAccepted", "false", "email", "CASuser@example.org"));
    }
    
    @Test
    public void determinePrincipalIdWithAdvancedConfig() {
        final String principalId = determinePrincipalId("casuser", CollectionUtils.wrap("aupAccepted", "false", "email", "CASuser@example.org"));
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
        final AcceptableUsagePolicyProperties aupProperties = casProperties.getAcceptableUsagePolicy();
        final JdbcAcceptableUsagePolicyRepository jdbcAupRepository = new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
                aupProperties.getAupAttributeName(), acceptableUsagePolicyDataSource, aupProperties);
        
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        final Principal pricipal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), profileAttributes);
        final Authentication auth = CoreAuthenticationTestUtils.getAuthentication(pricipal);
        WebUtils.putAuthentication(auth, context);
        
        jdbcAupRepository.determinePrincipalId(context, c);
    }
    
}
