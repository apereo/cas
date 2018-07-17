package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.FileSystemResource;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link GroovySamlRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(FileSystemCategory.class)
public class GroovySamlRegisteredServiceAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {

    @BeforeClass
    public static void beforeClass() {
        METADATA_DIRECTORY = new FileSystemResource(FileUtils.getTempDirectory());
    }

    @Test
    public void verifyScriptReleasesSamlAttributes() {
        val filter = new GroovySamlRegisteredServiceAttributeReleasePolicy();
        filter.setGroovyScript("classpath:saml-groovy-attrs.groovy");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        val registeredService = getSamlRegisteredServiceForTestShib();
        registeredService.setAttributeReleasePolicy(filter);
        final Map attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertFalse(attributes.isEmpty());
    }

}
