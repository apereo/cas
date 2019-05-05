package org.apereo.cas.mfa.simple;

import org.junit.platform.suite.api.SelectClasses;

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
public class AllCasSimpleMultifactorTestsSuite {
}
