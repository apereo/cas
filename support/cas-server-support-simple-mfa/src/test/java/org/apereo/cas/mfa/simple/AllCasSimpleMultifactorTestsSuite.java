package org.apereo.cas.mfa.simple;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllCasSimpleMultifactorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    CasSimpleMultifactorAuthenticationProviderTests.class,
    CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllCasSimpleMultifactorTestsSuite {
}
