package org.apereo.cas.authentication.handler.support;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * This is {@link AuthenticationHandlerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationHandlerUtils {
    protected AuthenticationHandlerUtils() {
    }

    /**
     * Gets credential selection predicate.
     *
     * @param selectionCriteria the selection criteria
     * @return the credential selection predicate
     */
    public static Predicate<Credential> newCredentialSelectionPredicate(final String selectionCriteria) {
        try {
            if (StringUtils.isBlank(selectionCriteria)) {
                return credential -> true;
            }
            final Class predicateClazz = ClassUtils.getClass(selectionCriteria);
            return (Predicate<Credential>) predicateClazz.newInstance();
        } catch (final Exception e) {
            final Predicate<String> predicate = Pattern.compile(selectionCriteria).asPredicate();
            return credential -> predicate.test(credential.getId());
        }
    }
}
