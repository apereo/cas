package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.Cleanup;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import javax.sql.DataSource;
import java.sql.Statement;

/**
 * This is {@link BaseJdbcAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = CasJdbcAuthenticationConfigurationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseJdbcAttributeRepositoryTests {
    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    protected PersonAttributeDao attributeRepository;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    protected DataSource dataSource;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Mock
    protected ServicesManager servicesManager;

    @Mock
    protected AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
    protected AttributeRepositoryResolver attributeRepositoryResolver;

    @BeforeEach
    void setupDatabase() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        val jdbc = casProperties.getAuthn().getAttributeRepository().getJdbc();
        if (!jdbc.isEmpty()) {
            this.dataSource = JpaBeans.newDataSource(jdbc.getFirst());
            @Cleanup
            val connection = dataSource.getConnection();
            @Cleanup
            val statement = connection.createStatement();
            connection.setAutoCommit(true);
            prepareDatabaseTable(statement);
        }
    }

    public void prepareDatabaseTable(final Statement statement) throws Exception {
    }

    @AfterEach
    public void cleanup() throws Exception {
        if (dataSource != null) {
            @Cleanup
            val connection = dataSource.getConnection();
            @Cleanup
            val statement = connection.createStatement();
            connection.setAutoCommit(true);
            statement.execute(String.format("delete from %s;", getTableName()));
            statement.execute(String.format("drop table %s;", getTableName()));
        }
    }

    protected String getTableName() {
        return "table_users";
    }
}
