package org.apereo.cas;

import org.apereo.cas.web.view.CasThymeleafConfigurationTests;
import org.apereo.cas.web.view.ChainingTemplateViewResolverTests;
import org.apereo.cas.web.view.RestfulUrlTemplateResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    ChainingTemplateViewResolverTests.class,
    RestfulUrlTemplateResolverTests.class,
    CasThymeleafConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
