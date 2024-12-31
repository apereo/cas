package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.config.CasSurrogateJdbcAuthenticationAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

/**
 * This is {@link SurrogateJdbcAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasSurrogateJdbcAuthenticationAutoConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.surrogate.jdbc.surrogate-search-query=select count(*) from surrogate_accounts where username=? and surrogateAccount=?",
    "cas.authn.surrogate.jdbc.surrogate-account-query=select * from surrogate_accounts where username=?",
    "cas.authn.surrogate.jdbc.auto-commit=true"
})
@Getter
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
class SurrogateJdbcAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService service;

    @Autowired
    @Qualifier("surrogateAuthenticationJdbcDataSource")
    private DataSource surrogateAuthenticationJdbcDataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void before() {
        jdbcTemplate = new JdbcTemplate(this.surrogateAuthenticationJdbcDataSource);
        jdbcTemplate.execute("drop table surrogate_accounts if exists;");
        jdbcTemplate.execute("create table surrogate_accounts (id int, username varchar(255), surrogateAccount varchar(255));");
        jdbcTemplate.execute("insert into surrogate_accounts values (100, 'casadmin', '" + SurrogateAuthenticationService.WILDCARD_ACCOUNT + "');");
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
