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
import org.junit.Test;
import org.junit.Before;
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

import static org.junit.Assert.*;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
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
@TestPropertySource(locations = {"classpath:/jdbc-aup.properties"})
public class JdbcAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Autowired
    @Qualifier("acceptableUsagePolicyDataSource")
    private DataSource acceptableUsagePolicyDataSource;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Before
    public void initialize() throws Exception {
        final var c = this.acceptableUsagePolicyDataSource.getConnection();
        final var s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("CREATE TABLE aup_table (id int primary key, username varchar(255), accepted boolean)");
        s.execute("INSERT INTO aup_table (id, username, accepted) values (100, 'casuser', false);");
        c.close();
    }

    @Test
    public void verifyAction() {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        final TicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser", c, CollectionUtils.wrap("accepted", "false"));
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertFalse(acceptableUsagePolicyRepository.verify(context, c).getLeft());
        assertTrue(acceptableUsagePolicyRepository.submit(context, c));
    }
}
