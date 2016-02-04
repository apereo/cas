package org.jasig.cas.util;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link ValidationTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class ValidationTestUtils {

    private static final String[] CONST_NO_PRINCIPALS = new String[0];

    private ValidationTestUtils() {}

    public static Assertion getAssertion(final boolean fromNewLogin) {
        return getAssertion(fromNewLogin, CONST_NO_PRINCIPALS);
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
                                         final String[] extraPrincipals) {
        final List<Authentication> list = new ArrayList<>(extraPrincipals.length + 1);
        list.add(AuthTestUtils.getAuthentication());

        list.addAll(Arrays.stream(extraPrincipals).map(AuthTestUtils::getAuthentication).collect(Collectors.toList()));
        return new ImmutableAssertion(AuthTestUtils.getAuthentication(),
                list, ServicesTestUtils.getService(), fromNewLogin);
    }

}
