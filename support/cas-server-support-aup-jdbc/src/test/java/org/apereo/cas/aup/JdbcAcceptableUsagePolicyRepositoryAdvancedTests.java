package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.config.CasAcceptableUsagePolicyJdbcConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.junit.After;

import static org.junit.Assert.*;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Martin Böhmer
 * @since 5.3.8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CasAcceptableUsagePolicyJdbcConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
})
@TestPropertySource(locations = {"classpath:/jdbc-aup-advanced.properties"})
public class JdbcAcceptableUsagePolicyRepositoryAdvancedTests {
    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Autowired
    @Qualifier("acceptableUsagePolicyDataSource")
    private DataSource acceptableUsagePolicyDataSource;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;
    
    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;
    
    @Autowired
    private CasConfigurationProperties casProperties;

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
    public void teardown() throws Exception {
        final Connection c = this.acceptableUsagePolicyDataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("DROP TABLE users_table;");
        c.close();
    }

    @Test
    public void verifyAction() {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        final Principal pricipal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), CollectionUtils.wrap("aupAccepted", "false", "email", "CASuser@example.org"));
        final Authentication auth = CoreAuthenticationTestUtils.getAuthentication(pricipal);
        WebUtils.putAuthentication(auth, context);
        // TGT can be deleted after merge of #3726
        final TicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser", c, CollectionUtils.wrap("aupAccepted", "false", "email", "CASuser@example.org"));
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertFalse(acceptableUsagePolicyRepository.verify(context, c).getLeft());
        assertTrue(acceptableUsagePolicyRepository.submit(context, c));
    }
    
    @Test
    public void testPrincipalIdDetermination() {
        final AcceptableUsagePolicyProperties aupProperties = casProperties.getAcceptableUsagePolicy();
        final JdbcAcceptableUsagePolicyRepository jdbcAupRepository = new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
                aupProperties.getAupAttributeName(), acceptableUsagePolicyDataSource, aupProperties);
        
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        final Principal pricipal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), CollectionUtils.wrap("aupAccepted", "false", "email", "CASuser@example.org"));
        final Authentication auth = CoreAuthenticationTestUtils.getAuthentication(pricipal);
        WebUtils.putAuthentication(auth, context);
        
        String principalId = jdbcAupRepository.determinePrincipalId(context, c, aupProperties.getJdbc());
        assertEquals("CASuser@example.org", principalId);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testMissingPrincipalAttribute() {
        final AcceptableUsagePolicyProperties aupProperties = casProperties.getAcceptableUsagePolicy();
        final JdbcAcceptableUsagePolicyRepository jdbcAupRepository = new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
                aupProperties.getAupAttributeName(), acceptableUsagePolicyDataSource, aupProperties);
        
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        final Principal pricipal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), CollectionUtils.wrap("aupAccepted", "false", "wrong-attribute", "CASuser@example.org"));
        final Authentication auth = CoreAuthenticationTestUtils.getAuthentication(pricipal);
        WebUtils.putAuthentication(auth, context);
        
        jdbcAupRepository.determinePrincipalId(context, c, aupProperties.getJdbc());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testBadTypePrincipalAttribute() {
        final AcceptableUsagePolicyProperties aupProperties = casProperties.getAcceptableUsagePolicy();
        final JdbcAcceptableUsagePolicyRepository jdbcAupRepository = new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
                aupProperties.getAupAttributeName(), acceptableUsagePolicyDataSource, aupProperties);
        
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        final Principal pricipal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), CollectionUtils.wrap("aupAccepted", "false", "email", 42));
        final Authentication auth = CoreAuthenticationTestUtils.getAuthentication(pricipal);
        WebUtils.putAuthentication(auth, context);
        
        jdbcAupRepository.determinePrincipalId(context, c, aupProperties.getJdbc());
    }
    
}
