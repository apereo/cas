package org.apereo.cas;

import org.apereo.cas.config.CasSamlSPAcademicHealthPlansConfigurationTests;
import org.apereo.cas.config.CasSamlSPAcademicWorksConfigurationTests;
import org.apereo.cas.util.SamlSPUtilsTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    CasSamlSPAcademicHealthPlansConfigurationTests.class,
    CasSamlSPAcademicWorksConfigurationTests.class,
    SamlSPUtilsTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
