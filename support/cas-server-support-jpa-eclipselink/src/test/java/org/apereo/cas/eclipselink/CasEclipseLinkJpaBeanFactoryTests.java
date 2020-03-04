package org.apereo.cas.eclipselink;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasEclipseLinkJpaConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEclipseLinkJpaBeanFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasEclipseLinkJpaConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = "cas.jdbc.showSql=true")
@Tag("JDBC")
@EnableTransactionManagement(proxyTargetClass = true)
public class CasEclipseLinkJpaBeanFactoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("jpaBeanFactory")
    private JpaBeanFactory jpaBeanFactory;

    @SneakyThrows
    private static DataSource dataSource() {
        return JpaBeans.newDataSource("org.hsqldb.jdbcDriver", "sa", StringUtils.EMPTY, "jdbc:hsqldb:mem:cas");
    }

    @Test
    public void verifyOperation() {
        val adapter = jpaBeanFactory.newJpaVendorAdapter();
        assertNotNull(adapter);

        val ctx = new JpaConfigurationContext(
            adapter,
            "sampleContext",
            CollectionUtils.wrap(SampleEntity.class.getPackage().getName()),
            dataSource());
        val bean = jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAudit().getJdbc());
        assertNotNull(bean);
    }

    @Entity
    @Getter
    @NoArgsConstructor
    private static class SampleEntity {
        @Id
        private long id;
    }
}
