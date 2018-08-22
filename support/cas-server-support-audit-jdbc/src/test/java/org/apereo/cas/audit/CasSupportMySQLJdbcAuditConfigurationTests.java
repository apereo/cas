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
@TestPropertySource(locations = "classpath:auditmysql.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 3306)
@Category(MySQLCategory.class)
public class CasSupportMySQLJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
