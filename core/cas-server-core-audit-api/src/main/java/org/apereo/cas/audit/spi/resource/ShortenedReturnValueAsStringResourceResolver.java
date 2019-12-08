package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.util.DigestUtils;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link ShortenedReturnValueAsStringResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ShortenedReturnValueAsStringResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        val resources = super.resolveFrom(auditableTarget, retval);
        if (resources != null) {
            return Arrays.stream(resources)
                .map(DigestUtils::abbreviate)
                .collect(Collectors.toList())
                .toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        }
        return null;
    }
}
