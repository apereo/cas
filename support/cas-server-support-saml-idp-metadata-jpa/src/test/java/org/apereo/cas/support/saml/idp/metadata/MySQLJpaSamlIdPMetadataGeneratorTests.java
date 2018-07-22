package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.category.MySQLCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(locations = "classpath:samlidp-mysql.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 3306)
@Category(MySQLCategory.class)
public class MySQLJpaSamlIdPMetadataGeneratorTests extends JpaSamlIdPMetadataGeneratorTests {
}
