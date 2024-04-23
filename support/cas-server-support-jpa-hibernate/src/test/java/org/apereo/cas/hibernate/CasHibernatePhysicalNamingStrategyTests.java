package org.apereo.cas.hibernate;

import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class
}, properties = {
    "cas.jdbc.physicalTableNames.CasHibernatePhysicalNamingStrategyTests=testtable",
    "cas.jdbc.physicalTableNames.GroovyTable=classpath:GroovyHibernatePhysicalNaming.groovy"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Hibernate")
class CasHibernatePhysicalNamingStrategyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyMappedTable() throws Throwable {
        val strategy = new CasHibernatePhysicalNamingStrategy();
        strategy.setApplicationContext(this.applicationContext);
        val id = strategy.toPhysicalTableName(
            Identifier.toIdentifier("CasHibernatePhysicalNamingStrategyTests"), mock(JdbcEnvironment.class));
        assertEquals("testtable", id.getText());
    }

    @Test
    void verifyMappedTableViaGroovy() throws Throwable {
        val strategy = new CasHibernatePhysicalNamingStrategy();
        strategy.setApplicationContext(this.applicationContext);
        val id = strategy.toPhysicalTableName(Identifier.toIdentifier("GroovyTable"), mock(JdbcEnvironment.class));
        assertEquals("CasTableName", id.getText());
        assertEquals("castablename", id.getCanonicalName());
    }
}
