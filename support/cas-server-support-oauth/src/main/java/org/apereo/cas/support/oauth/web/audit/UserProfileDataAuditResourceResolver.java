package org.apereo.cas.support.oauth.web.audit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

/**
 * The {@link UserProfileDataAuditResourceResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class UserProfileDataAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    @SuppressWarnings("unchecked")
    public String[] resolveFrom(JoinPoint auditableTarget, Object retval) {
        Objects.requireNonNull(retval, "User profile data Map<String, Object> must not be null");

        return new String[]{new ToStringBuilder(this, NO_CLASS_NAME_STYLE).append("user_profile_data", retval).toString()};
    }
}
