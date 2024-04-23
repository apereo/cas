package org.apereo.cas.locator;

import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.locator.TypeAwareGitRepositoryRegisteredServiceLocator;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TypeAwareGitRepositoryRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Git")
class TypeAwareGitRepositoryRegisteredServiceLocatorTests {
    @Test
    void verifyOperation() throws Throwable {
        val strategy = new DefaultRegisteredServiceResourceNamingStrategy();
        val locator = new TypeAwareGitRepositoryRegisteredServiceLocator(strategy,
            FileUtils.getTempDirectory(), new GitServiceRegistryProperties().setRootDirectory("sample-root"));
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val file = locator.determine(service, "json");
        assertTrue(file.getCanonicalPath().endsWith("sample-root" + File.separator + service.getFriendlyName() + File.separator + strategy.build(service, "json")));
    }
}
