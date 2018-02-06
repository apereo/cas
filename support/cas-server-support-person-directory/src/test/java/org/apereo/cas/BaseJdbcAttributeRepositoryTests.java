package org.apereo.cas;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * This is {@link BaseJdbcAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class})
public abstract class BaseJdbcAttributeRepositoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("attributeRepository")
    protected IPersonAttributeDao attributeRepository;

    protected DataSource dataSource;

    @Before
    @SneakyThrows
    public void setupDatabase() {
        this.dataSource = JpaBeans.newDataSource(casProperties.getAuthn().getAttributeRepository().getJdbc().get(0));
        @Cleanup final Connection c = dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);
        prepareDatabaseTable(s);
    }

    public abstract void prepareDatabaseTable(final Statement statement);

    @After
    @SneakyThrows
    public void cleanup() {
        @Cleanup final Connection c = dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("delete from table_users;");
        s.execute("drop table table_users;");
    }
}
