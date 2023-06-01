package org.apereo.cas;

import org.apereo.cas.nativex.CasNativeApplicationRunListenerTests;
import org.apereo.cas.nativex.CasNativeInfoContributorTests;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    CasNativeInfoContributorTests.class,
    CasNativeApplicationRunListenerTests.class
})
@Suite
public class AllTestsSuite {
}
