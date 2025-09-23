package org.apereo.cas.eclipselink;

import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasEclipseLinkJpaAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEclipseLinkJpaBeanFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasEclipseLinkJpaAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
}, properties = "cas.jdbc.show-sql=false")
@Tag("Hibernate")
@ExtendWith(CasTestExtension.class)
@EnableTransactionManagement(proxyTargetClass = false)
class CasEclipseLinkJpaBeanFactoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
    private JpaBeanFactory jpaBeanFactory;

    private static DataSource dataSource() {
        return JpaBeans.newDataSource("org.hsqldb.jdbcDriver", "sa", StringUtils.EMPTY, "jdbc:hsqldb:mem:cas");
    }

    @Test
    void verifyOperation() throws Throwable {
        val adapter = jpaBeanFactory.newJpaVendorAdapter();
        assertNotNull(adapter);

        val ctx = JpaConfigurationContext.builder()
            .dataSource(dataSource())
            .packagesToScan(CollectionUtils.wrapSet(SampleEntity.class.getPackage().getName()))
            .persistenceUnitName("sampleContext")
            .jpaVendorAdapter(adapter)
            .build();
        val bean = jpaBeanFactory.newEntityManagerFactoryBean(ctx,
            casProperties.getAudit().getJdbc()).getObject();
        assertNotNull(bean);
    }

    @Entity
    @Getter
    @NoArgsConstructor
    @SuppressWarnings("UnusedMethod")
    private static final class SampleEntity {
        @Id
        private long id;
    }
}
