package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.SurrogateJdbcAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import lombok.val;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogateJdbcAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    SurrogateJdbcAuthenticationConfiguration.class
})
@TestPropertySource(locations = {"classpath:/surrogate-jdbc.properties"})
public class SurrogateJdbcAuthenticationServiceTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService surrogateAuthenticationService;

    @Autowired
    @Qualifier("surrogateAuthenticationJdbcDataSource")
    private DataSource surrogateAuthenticationJdbcDataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        jdbcTemplate = new JdbcTemplate(this.surrogateAuthenticationJdbcDataSource);
        jdbcTemplate.execute("drop table surrogate_accounts if exists;");
        jdbcTemplate.execute("create table surrogate_accounts (id int, username varchar(255), surrogateAccount varchar(255));");
        jdbcTemplate.execute("insert into surrogate_accounts values (100, 'casuser', 'surrogate1');");
        jdbcTemplate.execute("insert into surrogate_accounts values (200, 'casuser', 'surrogate2');");
        jdbcTemplate.execute("insert into surrogate_accounts values (300, 'casuser', 'surrogate3');");
    }


    @Test
    public void verifyAccountsQualifying() {
        val results = surrogateAuthenticationService.getEligibleAccountsForSurrogateToProxy("casuser");
        assertFalse(results.isEmpty());
        assertEquals(3, results.size());
    }

    @Test
    public void verifyAccountQualifying() {
        val casuser = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val service = CoreAuthenticationTestUtils.getService();
        assertTrue(surrogateAuthenticationService.canAuthenticateAs("surrogate1", casuser, service));
        assertTrue(surrogateAuthenticationService.canAuthenticateAs("surrogate2", casuser, service));
        assertTrue(surrogateAuthenticationService.canAuthenticateAs("surrogate3", casuser, service));
    }
}
