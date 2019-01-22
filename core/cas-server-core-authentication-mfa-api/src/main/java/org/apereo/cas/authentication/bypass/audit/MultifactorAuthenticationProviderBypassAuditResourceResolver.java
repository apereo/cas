package org.apereo.cas.authentication.bypass.audit;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.util.AopUtils;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * This is {@link MultifactorAuthenticationProviderBypassAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class MultifactorAuthenticationProviderBypassAuditResourceResolver implements AuditResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {
        val jp = AopUtils.unWrapJoinPoint(joinPoint);
        val args = jp.getArgs();
        if (args != null) {
            val authn = (Authentication) args[0];
            val provider = (MultifactorAuthenticationProvider) args[2];
            val result = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("principal", authn.getPrincipal().getId())
                .append("provider", provider.getId())
                .append("execution", object)
                .toString();
            return new String[]{result};
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] resolveFrom(final JoinPoint target, final Exception exception) {
        return new String[]{target.getTarget().toString() + ": [" + exception.getMessage() + ']'};
    }
}
