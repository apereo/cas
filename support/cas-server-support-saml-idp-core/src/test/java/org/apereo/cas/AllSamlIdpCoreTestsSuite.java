package org.apereo.cas;

import org.apereo.cas.support.saml.services.ChainingWithDefaulAndSamlServicesManagersTests;
import org.apereo.cas.support.saml.services.SamlServicesManagerTests;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllSamlIdpCoreTestsSuite}.
 *
 * @author Dmitriy Kopylenko
 * @since 6.2.0
 */
@SelectClasses({
        ChainingWithDefaulAndSamlServicesManagersTests.class,
        SamlServicesManagerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllSamlIdpCoreTestsSuite {
}
