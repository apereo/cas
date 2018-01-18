package org.apereo.cas.validation;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link CoreValidationTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@UtilityClass
public class CoreValidationTestUtils {

    private static final String[] CONST_NO_PRINCIPALS = new String[0];
    
    public static Assertion getAssertion(final boolean fromNewLogin) {
        return getAssertion(fromNewLogin, CONST_NO_PRINCIPALS);
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
                                         final String[] extraPrincipals) {
        final List<Authentication> list = new ArrayList<>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        Arrays.stream(extraPrincipals).map(CoreAuthenticationTestUtils::getAuthentication).forEach(list::add);
        return new ImmutableAssertion(CoreAuthenticationTestUtils.getAuthentication(),
                list, fromNewLogin, RegisteredServiceTestUtils.getService());
    }

}
