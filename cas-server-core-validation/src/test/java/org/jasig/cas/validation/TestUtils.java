package org.jasig.cas.validation;

import org.jasig.cas.authentication.Authentication;

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
        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication());

        for (int i = 0; i < extraPrincipals.length; i++) {
            list.add(org.jasig.cas.authentication.TestUtils.getAuthentication(extraPrincipals[i]));
        }
        return new ImmutableAssertion(org.jasig.cas.authentication.TestUtils.getAuthentication(),
                list, org.jasig.cas.services.TestUtils.getService(), fromNewLogin);
    }

}
