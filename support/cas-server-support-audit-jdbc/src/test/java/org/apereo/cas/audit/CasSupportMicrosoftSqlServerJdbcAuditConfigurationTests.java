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
@TestPropertySource(locations = "classpath:auditmssql.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 1433)
@Category(MsSqlServerCategory.class)
public class CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
