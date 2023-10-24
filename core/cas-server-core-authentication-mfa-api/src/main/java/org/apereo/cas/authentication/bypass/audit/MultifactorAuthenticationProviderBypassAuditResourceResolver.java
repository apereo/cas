package org.apereo.cas.authentication.bypass.audit;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.util.AopUtils;

import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link MultifactorAuthenticationProviderBypassAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Setter
public class MultifactorAuthenticationProviderBypassAuditResourceResolver implements AuditResourceResolver {
    protected AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object returnValue) {
        val jp = AopUtils.unWrapJoinPoint(joinPoint);
        val args = jp.getArgs();
        if (args != null) {
            val authn = (Authentication) args[0];
            val provider = (MultifactorAuthenticationProvider) args[2];
            val values = new HashMap<String, Object>();
            values.put("principal", authn.getPrincipal().getId());
            values.put("provider", provider.getId());
            values.put("bypassed", BooleanUtils.toBoolean(returnValue.toString()));
            val bypass = (MultifactorAuthenticationProviderBypassEvaluator) jp.getTarget();
            values.put("source", bypass.getId());
            return new String[]{toResourceString(values)};
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] resolveFrom(final JoinPoint target, final Exception exception) {
        val values = new HashMap<String, Object>();
        values.put("target", target.getTarget().toString());
        values.put("exception", exception.getMessage());
        return new String[]{toResourceString(values)};
    }

    protected String toResourceString(final Map<String, Object> object) {
        return auditFormat.serialize(object);
    }
}
