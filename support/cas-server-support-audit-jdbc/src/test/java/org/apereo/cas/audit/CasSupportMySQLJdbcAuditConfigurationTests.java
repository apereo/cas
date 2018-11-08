package org.apereo.cas.audit;

import org.apereo.cas.category.MySQLCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSupportMySQLJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.audit.jdbc.user=root",
    "cas.audit.jdbc.password=password",
    "cas.audit.jdbc.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.audit.jdbc.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 3306)
@Category(MySQLCategory.class)
public class CasSupportMySQLJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
