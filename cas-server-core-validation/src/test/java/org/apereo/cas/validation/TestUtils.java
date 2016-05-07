package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link TestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TestUtils {

    private static final String[] CONST_NO_PRINCIPALS = new String[0];

    private TestUtils() {}

    public static Assertion getAssertion(final boolean fromNewLogin) {
        return getAssertion(fromNewLogin, CONST_NO_PRINCIPALS);
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
                                         final String[] extraPrincipals) {
        final List<Authentication> list = new ArrayList<>();
        list.add(org.apereo.cas.authentication.TestUtils.getAuthentication());

        for (final String extraPrincipal : extraPrincipals) {
            list.add(org.apereo.cas.authentication.TestUtils.getAuthentication(extraPrincipal));
        }
        return new ImmutableAssertion(org.apereo.cas.authentication.TestUtils.getAuthentication(),
                list, org.apereo.cas.services.TestUtils.getService(), fromNewLogin);
    }

}
