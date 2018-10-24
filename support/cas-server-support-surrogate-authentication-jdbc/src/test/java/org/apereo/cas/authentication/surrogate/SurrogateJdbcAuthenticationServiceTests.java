package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
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

import lombok.Getter;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;

import javax.sql.DataSource;

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
@TestPropertySource(properties = {
    "cas.authn.surrogate.jdbc.surrogateSearchQuery=select count(*) from surrogate_accounts where username=? and surrogateAccount=?",
    "cas.authn.surrogate.jdbc.surrogateAccountQuery=select * from surrogate_accounts where username=?",
    "cas.authn.surrogate.jdbc.autoCommit=true"
})
@Getter
public class SurrogateJdbcAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService service;

    @Autowired
    @Qualifier("surrogateAuthenticationJdbcDataSource")
    private DataSource surrogateAuthenticationJdbcDataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void before() {
        jdbcTemplate = new JdbcTemplate(this.surrogateAuthenticationJdbcDataSource);
        jdbcTemplate.execute("drop table surrogate_accounts if exists;");
        jdbcTemplate.execute("create table surrogate_accounts (id int, username varchar(255), surrogateAccount varchar(255));");
        jdbcTemplate.execute("insert into surrogate_accounts values (100, 'casuser', 'banderson');");
        jdbcTemplate.execute("insert into surrogate_accounts values (200, 'casuser', 'surrogate2');");
        jdbcTemplate.execute("insert into surrogate_accounts values (300, 'casuser', 'surrogate3');");
    }

    @AfterEach
    public void after() {
        jdbcTemplate = new JdbcTemplate(this.surrogateAuthenticationJdbcDataSource);
        jdbcTemplate.execute("drop table surrogate_accounts if exists;");
    }
}
