package org.apereo.cas;

import org.apereo.cas.web.flow.action.LoadSurrogatesListActionTests;
import org.apereo.cas.web.flow.action.SurrogateAuthorizationActionTests;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationActionTests;
import org.apereo.cas.web.flow.action.SurrogateSelectionActionTests;
import org.apereo.cas.web.flow.pac4j.SurrogateDelegatedAuthenticationCredentialExtractorTests;
import org.apereo.cas.web.flow.pac4j.SurrogateDelegatedAuthenticationPreProcessorTests;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationRequestParserTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link SurrogateTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    SurrogatePasswordlessAuthenticationRequestParserTests.class,
    SurrogateInitialAuthenticationActionTests.class,
    SurrogateDelegatedAuthenticationCredentialExtractorTests.class,
    SurrogateDelegatedAuthenticationPreProcessorTests.class,
    SurrogateSelectionActionTests.class,
    SurrogateAuthorizationActionTests.class,
    LoadSurrogatesListActionTests.class
})
@Suite
public class SurrogateTestsSuite {
}
