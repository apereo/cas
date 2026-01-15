package org.apereo.cas;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.ImmutableAssertion;
import lombok.experimental.UtilityClass;
import lombok.val;

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
        return getAssertion(fromNewLogin, extraPrincipals, CoreAuthenticationTestUtils.getRegisteredService());
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
                                         final String[] extraPrincipals,
                                         final RegisteredService registeredService) {
        val list = new ArrayList<Authentication>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        Arrays.stream(extraPrincipals).map(CoreAuthenticationTestUtils::getAuthentication).forEach(list::add);
        return new ImmutableAssertion(CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getAuthentication(),
            list, fromNewLogin, false, RegisteredServiceTestUtils.getService(), registeredService, Map.of());
    }

    public static Assertion getAssertion(final RegisteredService registeredService) {
        return getAssertion(false, CONST_NO_PRINCIPALS, registeredService);
    }
}
