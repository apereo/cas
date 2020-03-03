package org.apereo.cas.hibernate;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasHibernatePhysicalNamingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.jdbc.physicalTableNames.CasHibernatePhysicalNamingStrategyTests=testtable",
    "cas.jdbc.physicalTableNames.GroovyTable=classpath:GroovyHibernatePhysicalNaming.groovy"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("JDBC")
public class CasHibernatePhysicalNamingStrategyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyMappedTable() {
        val strategy = new CasHibernatePhysicalNamingStrategy();
        strategy.setApplicationContext(this.applicationContext);
        val id = strategy.toPhysicalTableName(
            Identifier.toIdentifier("CasHibernatePhysicalNamingStrategyTests"), mock(JdbcEnvironment.class));
        assertEquals("testtable", id.getText());
    }

    @Test
    public void verifyMappedTableViaGroovy() {
        val strategy = new CasHibernatePhysicalNamingStrategy();
        strategy.setApplicationContext(this.applicationContext);
        val id = strategy.toPhysicalTableName(Identifier.toIdentifier("GroovyTable"), mock(JdbcEnvironment.class));
        assertEquals("CasTableName", id.getText());
        assertEquals("castablename", id.getCanonicalName());
    }
}
