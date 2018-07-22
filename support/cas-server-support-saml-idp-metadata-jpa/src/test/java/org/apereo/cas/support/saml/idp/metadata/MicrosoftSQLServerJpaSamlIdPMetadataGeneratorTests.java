package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.category.MsSqlServerCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(locations = "classpath:samlidp-mssql.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 1433)
@Category(MsSqlServerCategory.class)
public class MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests extends JpaSamlIdPMetadataGeneratorTests {
}
