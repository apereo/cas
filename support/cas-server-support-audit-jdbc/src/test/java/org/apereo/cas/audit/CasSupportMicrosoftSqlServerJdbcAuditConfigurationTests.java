package org.apereo.cas.audit;

import org.apereo.cas.category.MsSqlServerCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.audit.jdbc.user=sa",
    "cas.audit.jdbc.password=p@ssw0rd",
    "cas.audit.jdbc.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.audit.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=master",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.SQLServer2012Dialect"
    })
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 1433)
@Category(MsSqlServerCategory.class)
public class CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
