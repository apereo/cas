package org.apereo.cas;

import org.apereo.cas.git.GitRepositoryBuilderTests;
import org.apereo.cas.git.GitRepositoryTests;
import org.apereo.cas.git.LoggingGitProgressMonitorTests;
import org.apereo.cas.git.PathRegexPatternTreeFilterTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    LoggingGitProgressMonitorTests.class,
    GitRepositoryBuilderTests.class,
    GitRepositoryTests.class,
    PathRegexPatternTreeFilterTests.class
})
@Suite
public class AllTestsSuite {
}
