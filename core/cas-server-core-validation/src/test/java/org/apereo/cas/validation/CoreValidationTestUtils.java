package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is {@link CoreValidationTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@UtilityClass
public class CoreValidationTestUtils {

    private static final String[] CONST_NO_PRINCIPALS = new String[0];

    public static Assertion getAssertion(final boolean fromNewLogin) {
        return getAssertion(fromNewLogin, CONST_NO_PRINCIPALS);
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
                                         final String[] extraPrincipals) {
        val list = new ArrayList<Authentication>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        Arrays.stream(extraPrincipals).map(CoreAuthenticationTestUtils::getAuthentication).forEach(list::add);
        return new ImmutableAssertion(CoreAuthenticationTestUtils.getAuthentication(),
            list, fromNewLogin, RegisteredServiceTestUtils.getService());
    }

}
