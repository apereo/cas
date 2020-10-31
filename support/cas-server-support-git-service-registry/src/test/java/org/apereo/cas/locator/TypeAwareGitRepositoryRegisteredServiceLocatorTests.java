package org.apereo.cas.locator;

import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.locator.TypeAwareGitRepositoryRegisteredServiceLocator;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TypeAwareGitRepositoryRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("FileSystem")
public class TypeAwareGitRepositoryRegisteredServiceLocatorTests {
    @Test
    public void verifyOperation() throws Exception {
        val strategy = new DefaultRegisteredServiceResourceNamingStrategy();
        val locator = new TypeAwareGitRepositoryRegisteredServiceLocator(strategy,
            FileUtils.getTempDirectory(), new GitServiceRegistryProperties().setRootDirectory("sample-root"));
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val file = locator.determine(service, "json");
        assertTrue(file.getCanonicalPath().endsWith("sample-root/" + service.getFriendlyName() + '/' + strategy.build(service, "json")));
    }
}
