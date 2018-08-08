package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.sql.DataSource;
import java.sql.Statement;

/**
 * This is {@link BaseJdbcAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class})
public abstract class BaseJdbcAttributeRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("attributeRepository")
    protected IPersonAttributeDao attributeRepository;

    protected DataSource dataSource;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Before
    @SneakyThrows
    public void setupDatabase() {
        this.dataSource = JpaBeans.newDataSource(casProperties.getAuthn().getAttributeRepository().getJdbc().get(0));
        @Cleanup
        val c = dataSource.getConnection();
        val s = c.createStatement();
        c.setAutoCommit(true);
        prepareDatabaseTable(s);
    }

    public abstract void prepareDatabaseTable(Statement statement);

    @After
    @SneakyThrows
    public void cleanup() {
        @Cleanup
        val c = dataSource.getConnection();
        val s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("delete from table_users;");
        s.execute("drop table table_users;");
    }
}
